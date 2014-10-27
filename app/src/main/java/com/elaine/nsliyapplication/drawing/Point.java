package com.elaine.nsliyapplication.drawing;

/**
 * Created by Elaine on 10/23/2014.
 */
public class Point {
    private final float x;
    private final float y;
    private final long timestamp;

    public Point(float x, float y){
        this.x = x;
        this.y = y;
        this.timestamp = 0;
    }

    public Point(float x, float y, long timestamp){
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }

    public static Point midPointOf(Point pt1, Point pt2){
        return new Point((pt1.getX() + pt2.getX())/2, (pt1.getY() + pt2.getY())/2);
    }

    public static Point linearInterp(Point pt1, Point pt2, float fraction){
        float diffX = pt2.getX() - pt1.getX();
        float diffY = pt2.getY() - pt1.getY();

        return new Point(pt1.getX() + fraction*diffX, pt1.getY() + fraction*diffY);
    }

    public float distanceTo(Point other){
        return (float) Math.sqrt(Math.pow(this.getX() - other.getX(),2)
                + Math.pow(this.getY() - other.getY(),2));
    }

    public float velocityFrom(Point start){
        return distanceTo(start)/(this.timestamp - start.timestamp);
    }
}
