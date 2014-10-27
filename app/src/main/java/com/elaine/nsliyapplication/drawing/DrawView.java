package com.elaine.nsliyapplication.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Based on Eric Burke's SquareUp post.
 * Created by Elaine on 10/23/2014.
 */
public class DrawView extends View {

    private static final float MIN_STROKE_WIDTH = 4f;
    private static final float MAX_STROKE_WIDTH = 20f;
    private static final float VELOCITY_FILTER_WEIGHT = .7f;
    private static final float MAX_VELOCITY = 6f;

    private float strokeWidth = MAX_STROKE_WIDTH;
    private float lastVelocity = 0f;
    private Paint paint = new Paint();

    Bitmap bitmap;
    Canvas canvas;
    private ArrayList<Bezier> beziers = new ArrayList<Bezier>();
    private ArrayList<Point> touchPoints = new ArrayList<Point>();

    private final RectF rectToUpdate = new RectF();

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f);
    }

    public void clear(){
        beziers.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(bitmap!=null) {
            Paint paint = new Paint();
            paint.setAlpha(150);
            canvas.drawBitmap(bitmap,0,0,null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(bitmap == null){
            bitmap = Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawColor(Color.TRANSPARENT);
        }

        float eventX = event.getX();
        float eventY = event.getY();

        boolean result;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                relocateRectToUpdate(eventX, eventY);
                lastVelocity = 0f;
                strokeWidth = MAX_STROKE_WIDTH;
                touchPoints = new ArrayList<Point>();
                touchPoints.add(new Point(eventX,eventY, event.getEventTime()));
                result = true;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                expandRectToUpdate(eventX, eventY);
                for(Point point : touchPoints){
                    expandRectToUpdate(point.getX(),point.getY());
                }
                int historySize = event.getHistorySize();

                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandRectToUpdate(historicalX, historicalY);
                    touchPoints.add(new Point(historicalX,historicalY,event.getHistoricalEventTime(i)));

                    int size = touchPoints.size();
                    if(size%2 == 1) {
                        float velocity = touchPoints.get(size - 1).
                                velocityFrom(touchPoints.get(size - 3));
                        velocity = VELOCITY_FILTER_WEIGHT*velocity +
                                (1-VELOCITY_FILTER_WEIGHT)*lastVelocity;
                        float newWidth = strokeWidth(velocity);
                        beziers.add(new Bezier(touchPoints.get(size - 3),
                                touchPoints.get(size - 2), touchPoints.get(size - 1),
                                strokeWidth,newWidth));
                        beziers.get(beziers.size() - 1).paintCurve(canvas,paint);
                        lastVelocity = velocity;
                        strokeWidth = newWidth;
                    }
                }

                touchPoints.add(new Point(eventX,eventY,event.getEventTime()));
                int size = touchPoints.size();
                if(size%2 ==1) {
                    float velocity = touchPoints.get(size - 1).
                            velocityFrom(touchPoints.get(size - 3));
                    velocity = VELOCITY_FILTER_WEIGHT*velocity +
                            (1-VELOCITY_FILTER_WEIGHT)*lastVelocity;
                    float newWidth = strokeWidth(velocity);
                    beziers.add(new Bezier(touchPoints.get(size - 3),
                            touchPoints.get(size - 2), touchPoints.get(size - 1),
                            strokeWidth,newWidth));
                    beziers.get(beziers.size() - 1).paintCurve(canvas,paint);
                    lastVelocity = velocity;
                    strokeWidth = newWidth;
                }

                invalidate(
                        (int) (rectToUpdate.left - MAX_STROKE_WIDTH),
                        (int) (rectToUpdate.top - MAX_STROKE_WIDTH),
                        (int) (rectToUpdate.right + MAX_STROKE_WIDTH),
                        (int) (rectToUpdate.bottom + MAX_STROKE_WIDTH)
                );

                if(event.getAction() == MotionEvent.ACTION_MOVE) {
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
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    if(size % 2 == 0){
                        float velocity = touchPoints.get(size - 1).
                                velocityFrom(touchPoints.get(size - 2));
                        velocity = VELOCITY_FILTER_WEIGHT*velocity +
                                (1-VELOCITY_FILTER_WEIGHT)*lastVelocity;
                        float newWidth = strokeWidth(velocity);
                        beziers.add(new Bezier(touchPoints.get(size - 2),touchPoints.get(size - 1),
                                strokeWidth,newWidth));
                        beziers.get(beziers.size() - 1).paintCurve(canvas,paint);
                        lastVelocity = velocity;
                        strokeWidth = newWidth;
                    }
                }
                relocateRectToUpdate(eventX, eventY);
                result = true;
                break;
            default:
                result = false;
        }

        return result;
    }

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

    // Reset rectangle to update given an event's location
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

    private void relocateRectToUpdate(float x, float y){
        rectToUpdate.left = x;
        rectToUpdate.right = x;
        rectToUpdate.top = y;
        rectToUpdate.bottom = y;
    }
}
