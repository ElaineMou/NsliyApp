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
 * Inspired  Eric Burke's SquareUp post.
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
     * Coordinates of last stroke.
     */
    RectF lastStroke = new RectF();
    /**
     * Bounds of character being written.
     */
    RectF characterBounds;
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
     * Current stroke image being drawn onto
     */
    Bitmap currentStroke;
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
        characterBounds = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(bitmap!=null) {
            canvas.drawBitmap(bitmap,0,0,null);
        }

        if(characterBounds!=null) {
            canvas.drawRect(characterBounds, redPaint);
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
        int eventAction = event.getAction();

        if(characterBounds == null){
            characterBounds = new RectF(eventX, eventY, eventX, eventY);
        }

        if(touchPoints.isEmpty() || eventAction != MotionEvent.ACTION_MOVE ||
                touchPoints.get(touchPoints.size() - 1).distanceTo(new Point(eventX,eventY)) > 3f) {

            // Depending on what type of event
            switch (eventAction) {
                // First touch down by user
                case MotionEvent.ACTION_DOWN:
                    // Create bitmap with transparency, new canvas to draw onto bitmap with.
                    currentStroke = Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(currentStroke);
                    canvas.drawColor(Color.TRANSPARENT);

                    // Relocate/clear dirty rectangle and current stroke bounds
                    relocateRect(lastStroke,eventX,eventY);
                    relocateRectToUpdate(eventX, eventY);
                    // Initialize stroke width and velocity
                    lastVelocity = 0f;
                    strokeWidth = MIN_STROKE_WIDTH;

                    // Empty touch points and add new event's coordinates
                    touchPoints = new ArrayList<Point>();
                    touchPoints.add(new Point(eventX, eventY, event.getEventTime()));
                    result = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    // If we have recorded a touch down before this move/touch up
                    if(!touchPoints.isEmpty()) {
                        // Expand dirty rectangle to new event coordinates
                        expandRectToUpdate(eventX, eventY);
                        // Expand dirty rectangle to any previous touch points
                        for (Point point : touchPoints) {
                            expandRectToUpdate(point.getX(), point.getY());
                        }
                        int historySize = event.getHistorySize();

                        // Expand dirty rectangle to all points in history
                        for (int i = 0; i < historySize; i++) {
                            float historicalX = event.getHistoricalX(i);
                            float historicalY = event.getHistoricalY(i);
                            expandRectToUpdate(historicalX, historicalY);
                            // Add all points in history to touch points
                            touchPoints.add(new Point(historicalX, historicalY, event.getHistoricalEventTime(i)));

                            // When touchPoints has odd number of points
                            int size = touchPoints.size();
                            if (size % 2 == 1) {
                                // Add Bezier curve between last three points,
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

                        // Add event point as last touch point if moving (avoid end blot)
                        if(eventAction == MotionEvent.ACTION_MOVE) {
                            touchPoints.add(new Point(eventX, eventY, event.getEventTime()));
                            int size = touchPoints.size();
                            // Add curve if we have reached an odd number of touch points
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
                                // Save last point if a curve to it has been completed
                                Point pointToSave = touchPoints.get(size - 1);
                                touchPoints = new ArrayList<Point>();
                                touchPoints.add(pointToSave);
                            // Save last two points if no curve has been made through them
                            } else if (size % 2 == 0) {
                                Point point1 = touchPoints.get(size - 2);
                                Point point2 = touchPoints.get(size - 1);
                                touchPoints = new ArrayList<Point>();
                                touchPoints.add(point1);
                                touchPoints.add(point2);
                            }
                            // Expand stroke bounds to include this event
                            expandRectWithRect(lastStroke,rectToUpdate);
                        // If finishing stroke
                        } else if (eventAction == MotionEvent.ACTION_UP) {
                            int size = touchPoints.size();
                            // Add linear line to final point if no curve has been made
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
                            // Expand stroke bounds to include this event
                            expandRectWithRect(lastStroke,rectToUpdate);
                            // Find new stroke's boundaries within borders
                            int x = (int) Math.max(lastStroke.left - MAX_STROKE_WIDTH, 0);
                            int y = (int) Math.max(lastStroke.top - MAX_STROKE_WIDTH, 0);
                            int newStrokeWidth = (int) Math.min(
                                    lastStroke.width() + 2*MAX_STROKE_WIDTH, getWidth() - x);
                            int newStrokeHeight = (int) Math.min(
                                    lastStroke.height() + 2*MAX_STROKE_WIDTH, getHeight() - y);
                            // Excise stroke bounds from bitmap
                            Bitmap newStroke = Bitmap.createBitmap(currentStroke,
                                    x,y, newStrokeWidth, newStrokeHeight);
                            // Add new smaller stroke bitmap
                            strokes.add(newStroke);
                            currentStroke = null;
                            // Expand character bounds to include this event
                            expandRectWithRect(characterBounds,lastStroke);
                            invalidate();
                        }

                        // Redraw the dirtied area
                        invalidate(
                                (int) (rectToUpdate.left - MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.top - MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.right + MAX_STROKE_WIDTH),
                                (int) (rectToUpdate.bottom + MAX_STROKE_WIDTH)
                        );
                        // Clear dirty rectangle
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
    }

    /**
     * Expands first rectangle to include 2nd rectangle's bounds.
     * @param rectangle1 - Rectangle to be expanded.
     * @param rectangle2 - Rectangle used as input.
     */
    private void expandRectWithRect(RectF rectangle1, RectF rectangle2){
        if(rectangle2.left < rectangle1.left) {
            rectangle1.left = rectangle2.left;
        }
        if (rectangle2.right > rectangle1.right){
            rectangle1.right = rectangle2.right;
        }
        if(rectangle2.top < rectangle1.top) {
            rectangle1.top = rectangle2.top;
        }
        if (rectangle2.bottom > rectangle1.bottom){
            rectangle1.bottom = rectangle2.bottom;
        }
    }

    /**
     * Moves rectangle to coordinates with no area.
     * @param rectangle - Rectangle to be relocated.
     * @param x - X coordinate to move to
     * @param y - Y coordinate to move to
     */
    private void relocateRect(RectF rectangle, float x, float y){
        rectangle.left = x;
        rectangle.right = x;
        rectangle.top = y;
        rectangle.bottom = y;
    }
}
