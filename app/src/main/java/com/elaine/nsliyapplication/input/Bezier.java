package com.elaine.nsliyapplication.input;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Bezier curve class.
 * Created by Elaine on 10/24/2014.
 */
public class Bezier {

    public static final int MAX_NUM_POINTS = 128;

    private final Point point1;
    private final Point controlPoint;
    private final Point point2;

    private final float startWidth;
    private final float endWidth;

    public Bezier(Point prev, Point current,float startWidth, float endWidth){
        this.point1 = prev;
        this.controlPoint = null;
        this.point2 = current;

        this.startWidth = startWidth;
        this.endWidth = endWidth;
    }

    public Bezier(Point prev2, Point prev1, Point current, float startWidth, float endWidth){
        this.point1 = prev2;
        this.controlPoint = prev1;
        this.point2 = current;

        this.startWidth = startWidth;
        this.endWidth = endWidth;
    }

    public void paintCurve(Canvas canvas, Paint paint){
        float widthDiff = endWidth - startWidth;
        float t;
        int i = 1;
        Paint circlePaint = new Paint(paint);
        circlePaint.setStyle(Paint.Style.FILL);

        paint.setStrokeWidth(startWidth);

        canvas.drawCircle(point1.getX(), point1.getY(), startWidth/2, circlePaint);

        if(controlPoint == null){
            int numberOfPoints = (int) (point1.distanceTo(point2));

            while(i<numberOfPoints){
                t = ((float)i)/numberOfPoints;
                Point newPoint = Point.linearInterp(point1,point2,t);

                paint.setStrokeWidth(startWidth + t*t*t*widthDiff);
                canvas.drawPoint(newPoint.getX(), newPoint.getY(), paint);
                i++;
            }
        } else {
            int numberOfPoints = (int) (point1.distanceTo(controlPoint) +
                    controlPoint.distanceTo(point2));
            if (numberOfPoints > Bezier.MAX_NUM_POINTS){
                numberOfPoints = Bezier.MAX_NUM_POINTS;
            }

            while(i<numberOfPoints){
                t = ((float)i)/numberOfPoints;

                Point midPt1 = Point.linearInterp(point1,controlPoint,t);
                Point midPt2 = Point.linearInterp(controlPoint,point2,t);

                Point newPoint = Point.linearInterp(midPt1,midPt2,t);

                paint.setStrokeWidth(startWidth + t*t*t*widthDiff);
                canvas.drawPoint(newPoint.getX(),newPoint.getY(),paint);
                i++;
            }
        }

        canvas.drawCircle(point2.getX(),point2.getY(),endWidth/2,circlePaint);

    }

}
