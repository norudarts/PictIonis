package etna.hyvernparede.pictionis.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Paint paint;
    private int lastX;
    private int lastY;

    // Canvas
    private Canvas buffer;
    private int width = 1480;
    private int height = 1080;
    private Bitmap bitmap;
    private Paint bitmapPaint;
    private int currentColor;
    private float scale = 1.0f;

    // Segments and Path
    private List<String> segmentIds;
    private Segment currentSegment;
    private List<Segment> drawnSegments;
    private Path path;

    // Firebase
    private DatabaseReference databaseReference;
    private DatabaseReference segmentsReference;
    private ChildEventListener listener;

    // Constants
    public static final String TAG = "DrawingView";
    public static final String SEGMENTS_CHILD = "segments";
    public static final int PIXEL_SIZE = 12;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.setBackgroundColor(Color.WHITE);

        path = new Path();
        paint = makePaint();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        currentColor = Color.BLACK;
        segmentIds = new ArrayList<>();
        drawnSegments = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        segmentsReference = databaseReference.child(SEGMENTS_CHILD);
        listener = segmentsReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String newSegmentId = dataSnapshot.getKey();
                if (!segmentIds.contains(newSegmentId)) {
                    Segment segment = dataSnapshot.getValue(Segment.class);
                    drawSegment(segment, makePaint(segment.getColor()));

                    drawnSegments.add(segment);
                    segmentIds.remove(newSegmentId);
                    // Draw
                    invalidate();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                clearScreen();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Touch methods
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp(x, y);
                invalidate();
                break;
        }

        return true;
    }

    private void onTouchDown(float x, float y) {
        // Initiate segment
        path.reset();

        path.moveTo(x, y);
        currentSegment = new Segment(currentColor);

        lastX = (int) x / PIXEL_SIZE;
        lastY = (int) y / PIXEL_SIZE;
        currentSegment.addPoint(lastX, lastY);
    }

    private void onTouchMove(float x, float y) {
        // Build segment
        int posx = (int) x / PIXEL_SIZE;
        int posy = (int) y / PIXEL_SIZE;

        float distx = Math.abs(posx - lastX);
        float disty = Math.abs(posy - lastY);

        if (distx >= 1 || disty >= 1) {
            path.quadTo(lastX * PIXEL_SIZE, lastY * PIXEL_SIZE,
                    ((posx + lastX) * PIXEL_SIZE) / 2, ((posy + lastY) * PIXEL_SIZE) / 2);

            lastX = posx;
            lastY = posy;

            currentSegment.addPoint(lastX, lastY);
        }
    }

    private void onTouchUp(float x, float y) {
        // Close segment, draw and push to Firebase DB
        path.lineTo(lastX * PIXEL_SIZE, lastY * PIXEL_SIZE);
        buffer.drawPath(path, paint);
        path.reset();
        drawnSegments.add(currentSegment);

        DatabaseReference newSegmentReference = segmentsReference.push();
        final String segmentId = segmentsReference.getKey();
        segmentIds.add(segmentId);

        // Scaling
        Segment scaledSegment = new Segment(currentSegment.getColor());
        for (Point point: currentSegment.getPoints()) {
            scaledSegment.addPoint(
                    Math.round(point.getX() / scale), Math.round(point.getY() / scale)
            );
        }

        newSegmentReference.setValue(scaledSegment, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, databaseError.toString());
                }

//                segmentIds.remove(segmentId);
            }
        });
    }

    // Drawing methods
    public void drawSegment(Segment segment, Paint paint) {
        if (buffer != null) {
            buffer.drawPath(getPath(segment.getPoints()), paint);
        }
    }

    public Path getPath(List<Point> points) {
        Path path = new Path();
        float size = scale * PIXEL_SIZE;

        // Set initial position
        Point currentPoint = points.get(0);
        path.moveTo(Math.round(currentPoint.getX() * size), Math.round(currentPoint.getY() * size));
        Point nextPoint = null;

        // Move
        for (int i = 1; i < points.size(); i++) {
            nextPoint = points.get(i);

            path.quadTo(Math.round(currentPoint.getX() * size),
                    Math.round(currentPoint.getY() * size),
                    Math.round((currentPoint.getX() + nextPoint.getX()) * size / 2),
                    Math.round((currentPoint.getY() + nextPoint.getY()) * size / 2));

            currentPoint = nextPoint;
        }
        if (nextPoint != null) {
            path.lineTo(Math.round(nextPoint.getX() * size), Math.round(nextPoint.getY() * size));
        }

        return path;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        scale = Math.min(1.0f * width / this.width, 1.0f * height / this.height);

        bitmap = Bitmap.createBitmap(Math.round(this.width * scale), Math.round(this.height * scale),
                Bitmap.Config.ARGB_8888);
        buffer = new Canvas(bitmap);
        for (Segment segment : drawnSegments) drawSegment(segment, makePaint(segment.getColor()));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.DKGRAY);
        canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(),
                makePaint(Color.WHITE, Paint.Style.FILL_AND_STROKE));
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.drawPath(path, paint);
    }

    public void clean() {
        // Reset Firebase and clear screen
        segmentsReference.removeEventListener(listener);
        segmentsReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                clearScreen();
            }
        });
    }

    private void clearScreen() {
        bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        buffer = new Canvas(bitmap);
        currentSegment = null;
        segmentIds.clear();
        drawnSegments.clear();

        invalidate();
    }

    // Paint methods
    public Paint makePaint() {
        return makePaint(Color.BLACK, Paint.Style.STROKE);
    }

    public Paint makePaint(int color) {
        return makePaint(color, Paint.Style.STROKE);
    }

    public Paint makePaint(int color, Paint.Style style) {
        Paint p = new Paint();

        p.setColor(color);
        p.setStyle(style);

        p.setAntiAlias(true);
        p.setDither(true);

        return p;
    }
}
