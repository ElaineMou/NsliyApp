package com.elaine.nsliyapplication.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Based on Eric Burke's SquareUp post.
 * Created by Elaine on 10/23/2014.
 */
public class DrawView extends View {

    /**
     * Minimum width for ink strokes.
     */
    private static final float MIN_STROKE_WIDTH = 15f;
    /**
     * Maximum width of ink strokes.
     */
    private static final float MAX_STROKE_WIDTH = 45f;
    /**
     * Degree of previous events' velocity affecting current width.
     */
    private static final float VELOCITY_FILTER_WEIGHT = .7f;
    /**
     * Maximum velocity received by view.
     */
    private static final float MAX_VELOCITY = 6f;

    /**
     * Width of drawn strokes.
     */
    private float strokeWidth = MIN_STROKE_WIDTH;
    /**
     * Velocity recorded at previous touch event.
     */
    private float lastVelocity = 0f;
    /**
     * Coordinates of last touch down.
     */
    Point lastTouchDown;
    /**
     * Paint used to draw.
     */
    private Paint paint = new Paint();

    /**
     * Paint used for debugging.
     */
    private Paint redPaint = new Paint();
    /**
     * Bitmap for displaying to user.
     */
    Bitmap bitmap;
    /**
     * Canvas used for drawing to single bitmap.
     */
    Canvas singleCanvas;
    /**
     * Bitmap images being written to.
     */
    ArrayList<Bitmap> strokes = new ArrayList<Bitmap>();
    /**
     * Canvas used to draw onto bitmap.
     */
    Canvas canvas;
    /**
     * List of curves drawn into a stroke.
     */
    private ArrayList<Bezier> beziers = new ArrayList<Bezier>();
    /**
     * List of touch points laid down in a single stroke.
     */
    private ArrayList<Point> touchPoints = new ArrayList<Point>();

    /**
     * The dirty rectangle to update the drawing on.
     */
    private final RectF rectToUpdate = new RectF();

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize paint values
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f);

        redPaint = new Paint(paint);
        redPaint.setColor(Color.RED);
    }

    // Clear screen
    public void clear(){
        beziers.clear();
        touchPoints.clear();
        bitmap = null;
        strokes.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(bitmap!=null) {
            canvas.drawBitmap(bitmap,0,0,null);
        }
    }

    /**
     * Handles motion/touch events from user.
     * @param event - MotionEvent from user.
     * @return - Success or failure.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(bitmap == null) {
            // Create bitmap with transparency, new canvas to draw onto bitmap with.
            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            singleCanvas = new Canvas(bitmap);
            singleCanvas.drawColor(Color.TRANSPARENT);
        }

        // boolean to return
        boolean result = true;

        // Save event's coordinates
        float eventX = event.getX();
        float eventY = event.getY();

        if(touchPoints.isEmpty() || touchPoints.get(touchPoints.size() - 1).distanceTo(new Point(eventX,eventY)) > 3f) {

            // Depending on what type of event
            switch (event.getAction()) {
                // First touch down by user
                case MotionEvent.ACTION_DOWN:
                    // Create bitmap with transparency, new canvas to draw onto bitmap with.
                    Bitmap stroke = Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(stroke);
                    canvas.drawColor(Color.TRANSPARENT);
                    strokes.add(stroke);

                    relocateRectToUpdate(eventX, eventY);
                    lastVelocity = 0f;
                    strokeWidth = MIN_STROKE_WIDTH;
                    touchPoints = new ArrayList<Point>();
                    touchPoints.add(new Point(eventX, eventY, event.getEventTime()));
                    result = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    if(!touchPoints.isEmpty()) {
                        expandRectToUpdate(eventX, eventY);
                        for (Point point : touchPoints) {
                            expandRectToUpdate(point.getX(), point.getY());
                        }
                        int historySize = event.getHistorySize();

                        for (int i = 0; i < historySize; i++) {
                            float historicalX = event.getHistoricalX(i);
                            float historicalY = event.getHistoricalY(i);
                            expandRectToUpdate(historicalX, historicalY);
                            touchPoints.add(new Point(historicalX, historicalY, event.getHistoricalEventTime(i)));

                            int size = touchPoints.size();
                            if (size % 2 == 1) {
                                float velocity = touchPoints.get(size - 1).
                                        velocityFrom(touchPoints.get(size - 3));
                                velocity = VELOCITY_FILTER_WEIGHT * velocity +
                                        (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
                                float newWidth = strokeWidth(velocity);
                                beziers.add(new Bezier(touchPoints.get(size - 3),
                                        touchPoints.get(size - 2), touchPoints.get(size - 1),
                                        strokeWidth, newWidth));
                                beziers.get(beziers.size() - 1).paintCurve(canvas, paint);
                                beziers.get(beziers.size() - 1).paintCurve(singleCanvas, paint);
                                lastVelocity = velocity;
                                strokeWidth = newWidth;
                            }
                        }

                        touchPoints.add(new Point(eventX, eventY, event.getEventTime()));
                        int size = touchPoints.size();
                        if (size % 2 == 1) {
                            float velocity = touchPoints.get(size - 1).
                                    velocityFrom(touchPoints.get(size - 3));
                            velocity = VELOCITY_FILTER_WEIGHT * velocity +
                                    (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
                            float newWidth = strokeWidth(velocity);
                            beziers.add(new Bezier(touchPoints.get(size - 3),
                                    touchPoints.get(size - 2), touchPoints.get(size - 1),
                                    strokeWidth, newWidth));
                            beziers.get(beziers.size() - 1).paintCurve(canvas, paint);
                            beziers.get(beziers.size() - 1).paintCurve(singleCanvas, paint);
                            lastVelocity = velocity;
                            strokeWidth = newWidth;
                        }

                        invalidate(
                                (int) (rectToUpdate.left - MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.top - MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.right + MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.bottom + MAX_STROKE_WIDTH)
                        );

                        if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            if (size % 2 == 1) {
                                Point pointToSave = touchPoints.get(size - 1);
                                touchPoints = new ArrayList<Point>();
                                touchPoints.add(pointToSave);
                            } else if (size % 2 == 0) {
                                Point point1 = touchPoints.get(size - 2);
                                Point point2 = touchPoints.get(size - 1);
                                touchPoints = new ArrayList<Point>();
                                touchPoints.add(point1);
                                touchPoints.add(point2);
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (size % 2 == 0) {
                                float velocity = touchPoints.get(size - 1).
                                        velocityFrom(touchPoints.get(size - 2));
                                velocity = VELOCITY_FILTER_WEIGHT * velocity +
                                        (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
                                float newWidth = strokeWidth(velocity);
                                beziers.add(new Bezier(touchPoints.get(size - 2), touchPoints.get(size - 1),
                                        strokeWidth, 0));
                                beziers.get(beziers.size() - 1).paintCurve(canvas, paint);
                                beziers.get(beziers.size() - 1).paintCurve(singleCanvas, paint);
                                lastVelocity = velocity;
                                strokeWidth = newWidth;
                            }
                        }
                        relocateRectToUpdate(eventX, eventY);
                    }
                    result = true;
                    break;
                default:
                    result = false;
            }
        }

        return result;
    }

    /**
     * Calculates stroke width based on velocity input.
     * @param velocity - Input velocity
     * @return - Width of stroke to be drawn
     */
    private float strokeWidth(float velocity) {
        float width = MIN_STROKE_WIDTH;
        if(velocity < MAX_VELOCITY){
            width = MIN_STROKE_WIDTH +
                    (MAX_STROKE_WIDTH - MIN_STROKE_WIDTH)*(1 - velocity/MAX_VELOCITY);
        } else {
            width = MIN_STROKE_WIDTH;
        }

        return width;
    }

    /**
     * Expands rectangle to include new point.
     * @param x - X coordinate of new point
     * @param y - Y coordinate of new point
     */
    private void expandRectToUpdate(float x, float y){
        if(x < rectToUpdate.left){
            rectToUpdate.left = x;
        } else if (x > rectToUpdate.right){
            rectToUpdate.right = x;
        }
        if(y < rectToUpdate.top){
            rectToUpdate.top = y;
        } else if (y > rectToUpdate.bottom){
            rectToUpdate.bottom = y;
        }

        Log.v("expandToRect",rectToUpdate.toString());
    }

    /**
     * Reset rectangle to a new location with 0 area.
     * @param x - X coordinate of rectangle
     * @param y - Y coordinate of rectangle.
     */
    private void relocateRectToUpdate(float x, float y){
        rectToUpdate.left = x;
        rectToUpdate.right = x;
        rectToUpdate.top = y;
        rectToUpdate.bottom = y;
        Log.v("relocateRect",rectToUpdate.toString());
    }
}
