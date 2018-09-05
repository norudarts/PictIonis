package etna.hyvernparede.pictionis.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class DrawingView extends View {

    public static final int PIXEL_SIZE = 8;

    private Paint paint;
    private int lastX;
    private int lastY;

    // Canvas
    private Canvas buffer;
    private int width;
    private int height;
    private Bitmap bitmap;
    private Paint bitmapPaint;
    private Color colorSelected;

    // Segments and Path
    private List<String> segmentIds;
    private Segment currentSegment;
    private Path path;

    // Firebase
    private DatabaseReference databaseReference;
    private ChildEventListener listener;
    private float scale = 1.0f;
    private int canvasWidth = 1080;
    private int canvasHeight = 1080;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

//    public DrawingView(Context context, DatabaseReference databaseReference) {
//        super(context,null);
//
//        this.databaseReference = databaseReference;
//        this.segmentIds = new ArrayList<>();
//
//        this.listener = databaseReference.addChildEventListener(new ChildEventListener() {
//
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                if (segmentIds.contains(dataSnapshot.getKey())) {
//                    Segment segment = dataSnapshot.getValue(Segment.class);
//                    drawSegment(segment, makePaint(segment.getColor()));
//
//                    // Draw
//                    invalidate();
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

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
        path.reset();
        path.moveTo(x, y);
        currentSegment = new Segment();
        lastX = (int) x / PIXEL_SIZE;
        lastY = (int) y / PIXEL_SIZE;
        currentSegment.addPoint(lastX, lastY);
    }

    private void onTouchMove(float x, float y) {

        int x1 = (int) x / PIXEL_SIZE;
        int y1 = (int) y / PIXEL_SIZE;

        float dx = Math.abs(x1 - lastX);
        float dy = Math.abs(y1 - lastY);
        if (dx >= 1 || dy >= 1) {
            path.quadTo(lastX * PIXEL_SIZE, lastY * PIXEL_SIZE, ((x1 + lastX) * PIXEL_SIZE) / 2, ((y1 + lastY) * PIXEL_SIZE) / 2);
            lastX = x1;
            lastY = y1;
            currentSegment.addPoint(lastX, lastY);
        }
    }

    private void onTouchUp(float x, float y) {
        path.lineTo(lastX * PIXEL_SIZE, lastY * PIXEL_SIZE);
        buffer.drawPath(path, paint);
        path.reset();
//        Firebase segmentRef = mFirebaseRef.push();
//        final String segmentName = segmentRef.getKey();
//        mOutstandingSegments.add(segmentName);

        // create a scaled version of the segment, so that it matches the size of the board
//        Segment segment = new Segment(mCurrentSegment.getColor());
//        for (Point point: mCurrentSegment.getPoints()) {
//            segment.addPoint((int)Math.round(point.x / mScale), (int)Math.round(point.y / mScale));
//        }

        // Save our segment into Firebase. This will let other clients see the data and add it to their own canvases.
        // Also make a note of the outstanding segment name so we don't do a duplicate draw in our onChildAdded callback.
        // We can remove the name from mOutstandingSegments once the completion listener is triggered, since we will have
        // received the child added event by then.
//        segmentRef.setValue(segment, new Firebase.CompletionListener() {
//            @Override
//            public void onComplete(FirebaseError error, Firebase firebaseRef) {
//                if (error != null) {
//                    Log.e("AndroidDrawing", error.toString());
//                    throw error.toException();
//                }
//                mOutstandingSegments.remove(segmentName);
//            }
//        });
    }

    // Drawing methods
    public void drawSegment(Segment segment, Paint paint) {
        if (buffer == null) {
            buffer.drawPath(getPath(segment.getPoints()), paint);
        }
    }

    public Path getPath(List<Point> points) {
        Path path = new Path();

        // Set initial position
        Point currentPoint = points.get(0);
        path.moveTo(currentPoint.getX() * PIXEL_SIZE, currentPoint.getY() * PIXEL_SIZE);
        Point nextPoint = null;

        // Move
        for (int i = 1; i < points.size(); i++) {
            nextPoint = points.get(i);

            path.quadTo(currentPoint.getX() * PIXEL_SIZE, currentPoint.getY() * PIXEL_SIZE,
                    (currentPoint.getX() + nextPoint.getX()) * PIXEL_SIZE,
                    (currentPoint.getY() + nextPoint.getY()) * PIXEL_SIZE);

            currentPoint = nextPoint;
        }
        if (nextPoint != null) {
            path.lineTo(nextPoint.getX() * PIXEL_SIZE, nextPoint.getY() * PIXEL_SIZE);
        }

        return path;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        scale = Math.min(1.0f * w / canvasWidth, 1.0f * h / canvasHeight);

        bitmap = Bitmap.createBitmap(Math.round(canvasWidth * scale), Math.round(canvasHeight * scale), Bitmap.Config.ARGB_8888);
        buffer = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(),
                makePaint(Color.WHITE, Paint.Style.FILL_AND_STROKE));
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.drawPath(path, paint);
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

        return p;
    }
}
