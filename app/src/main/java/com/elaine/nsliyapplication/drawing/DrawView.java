package com.elaine.nsliyapplication.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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

    private static final float STROKE_WIDTH = 5f;

    private Paint paint = new Paint();
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
        for(Bezier bezier : beziers){
            canvas.drawPath(bezier,paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        float eventX = event.getX();
        float eventY = event.getY();

        boolean result;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                relocateRectToUpdate(eventX, eventY);
                touchPoints = new ArrayList<Point>();
                touchPoints.add(new Point(eventX,eventY));
                result = true;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                expandRectToUpdate(eventX, eventY);

                int historySize = event.getHistorySize();
                if(historySize == 0){
                    touchPoints.add(new Point(eventX,eventY));
                    int size = touchPoints.size();
                    beziers.add(new Bezier(touchPoints.get(size - 2),touchPoints.get(size - 1)));
                } else {
                    touchPoints.add(new Point(event.getHistoricalX(0),event.getHistoricalY(0)));
                    for (int i = 1; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandRectToUpdate(historicalX, historicalY);
                        touchPoints.add(new Point(historicalX,historicalY));
                        int size = touchPoints.size();
                        beziers.add(new Bezier(touchPoints.get(size-3),
                                touchPoints.get(size - 2),touchPoints.get(size - 1)));
                    }
                }

                invalidate(
                        (int) (rectToUpdate.left - STROKE_WIDTH/2),
                        (int) (rectToUpdate.top - STROKE_WIDTH/2),
                        (int) (rectToUpdate.right + STROKE_WIDTH/2),
                        (int) (rectToUpdate.bottom + STROKE_WIDTH/2)
                );

                touchPoints = new ArrayList<Point>();
                touchPoints.add(new Point(eventX,eventY));
                relocateRectToUpdate(eventX, eventY);
                result = true;
                break;
            default:
                result = false;
        }

        return result;
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
