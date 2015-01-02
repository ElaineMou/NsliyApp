package com.elaine.nsliyapplication.input;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Bezier curve class for drawing varying-width strokes.
 * Created by Elaine on 10/24/2014.
 */
public class Bezier {

    /**
     * Maximum number of points to be drawn in a single curve.
     */
    public static final int MAX_NUM_POINTS = 128;

    /**
     * Beginning point of curve.
     */
    private final Point point1;
    /**
     * Point used to calculate curve (may be null).
     */
    private final Point controlPoint;
    /**
     * Ending point of curve.
     */
    private final Point point2;

    /**
     * Beginning width of stroke.
     */
    private final float startWidth;
    /**
     * Ending width of stroke.
     */
    private final float endWidth;

    /**
     * Creates a straight line with varying width.
     * @param start - Beginning point
     * @param end - Ending point
     * @param startWidth - Beginning width
     * @param endWidth - Ending width
     */
    public Bezier(Point start, Point end,float startWidth, float endWidth){
        this.point1 = start;
        this.controlPoint = null;
        this.point2 = end;

        this.startWidth = startWidth;
        this.endWidth = endWidth;
    }

    /**
     * Creates a curve of varying width between the given points.
     * @param start - Beginning point of curve.
     * @param control - Control point to calculate Bezier curve.
     * @param end - Ending point of curve.
     * @param startWidth - Beginning width of curve
     * @param endWidth - Ending width of curve.
     */
    public Bezier(Point start, Point control, Point end, float startWidth, float endWidth){
        this.point1 = start;
        this.controlPoint = control;
        this.point2 = end;

        this.startWidth = startWidth;
        this.endWidth = endWidth;
    }

    /**
     * Paints a calculated Bezier quadratic curve onto the given canvas.
     * @param canvas - Canvas to be drawn onto.
     * @param paint - Paint to be used.
     */
    public void paintCurve(Canvas canvas, Paint paint){
        float widthDiff = endWidth - startWidth;
        float t;
        int i = 1;

        // Draw initial circle point to round off stroke edge.
        Paint circlePaint = new Paint(paint);
        circlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(point1.getX(), point1.getY(), startWidth/2, circlePaint);

        paint.setStrokeWidth(startWidth);
        // If linear curve
        if(controlPoint == null){
            int numberOfPoints = (int) (point1.distanceTo(point2));
            if (numberOfPoints > MAX_NUM_POINTS){
                numberOfPoints = MAX_NUM_POINTS;
            }
            // Draw points along line while width increases from start to end in cubic fashion
            while(i<numberOfPoints){
                t = ((float)i)/numberOfPoints;
                Point newPoint = Point.linearInterp(point1,point2,t);

                paint.setStrokeWidth(startWidth + t*t*t*widthDiff);
                canvas.drawPoint(newPoint.getX(), newPoint.getY(), paint);
                i++;
            }
        } else {// If quadratic curve
            // Calculate number of points
            int numberOfPoints = (int) (point1.distanceTo(controlPoint) +
                    controlPoint.distanceTo(point2));
            if (numberOfPoints > MAX_NUM_POINTS){
                numberOfPoints = MAX_NUM_POINTS;
            }
            // Calculate a quadratic curve using the control point
            while(i<numberOfPoints){
                t = ((float)i)/numberOfPoints;

                Point midPt1 = Point.linearInterp(point1,controlPoint,t);
                Point midPt2 = Point.linearInterp(controlPoint,point2,t);

                Point newPoint = Point.linearInterp(midPt1,midPt2,t);
                // Adjust width as curve is drawn
                paint.setStrokeWidth(startWidth + t*t*t*widthDiff);
                canvas.drawPoint(newPoint.getX(),newPoint.getY(),paint);
                i++;
            }
        }

        canvas.drawCircle(point2.getX(),point2.getY(),endWidth/2,circlePaint);

    }

}
