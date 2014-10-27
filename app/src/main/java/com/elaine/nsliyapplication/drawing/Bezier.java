package com.elaine.nsliyapplication.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;

/**
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
        float t = 0.0f;
        int i = 1;

        canvas.drawPoint(point1.getX(),point1.getY(),paint);

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
        canvas.drawPoint(point2.getX(),point2.getY(),paint);
    }

/*    public Bezier(Point prev2, Point prev1,Point current){
        Point midPt1 = Point.midPointOf(prev2,prev1);
        Point midPt2 = Point.midPointOf(prev1,current);

        float distance = midPt1.distanceTo(midPt2);
        int numberOfSegments = 32;

        this.moveTo(prev2.getX(),prev2.getY());

        float t = 0.0f;
        for (int i=0;i<numberOfSegments;i++){
            t = ((float)i)/numberOfSegments;
            float tt = t*t;
            float u = 1-t;
            float uu = u*u;

            float x = uu*prev2.getX();
            x += 2*u*t*prev1.getX();
            x += tt*current.getX();

            float y = uu*prev2.getY();
            y += 2*u*t*prev1.getY();
            y += tt*current.getY();

            this.lineTo(x,y);
        }
    }*/

    public Point getPoint1(){
        return point1;
    }

    public Point getControlPoint(){
        return controlPoint;
    }

    public Point getPoint2(){
        return point2;
    }

    public float getStartWidth(){
        return startWidth;
    }

    public float getEndWidth(){
        return endWidth;
    }
}
