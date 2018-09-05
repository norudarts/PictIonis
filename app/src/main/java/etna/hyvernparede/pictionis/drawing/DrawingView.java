package etna.hyvernparede.pictionis.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
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

    // Segments and Path
    private List<String> segmentIds;
    private Segment currentSegment;
    private Path path;

    // Firebase
    private DatabaseReference databaseReference;
    private ChildEventListener listener;

    public DrawingView(Context context, DatabaseReference databaseReference) {
        super(context);

        this.databaseReference = databaseReference;
        this.segmentIds = new ArrayList<String>();

        this.listener = databaseReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (segmentIds.contains(dataSnapshot.getKey())) {
                    Segment segment = dataSnapshot.getValue(Segment.class);
                    drawSegment(segment, makePaint(segment.getColor()));

                    // Draw
                    invalidate();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

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

    public void onTouchDown(float x, float y) {
        // TODO Initiate segment
    }

    public void onTouchMove(float x, float y) {
        // TODO Build segment
    }

    public void onTouchUp(float x, float y) {
        // TODO Push segment to Firebase DB
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
