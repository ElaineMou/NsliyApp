package com.elaine.nsliyapplication.input;

/**
 * Point used for drawing.
 * Created by Elaine on 10/23/2014.
 */
public class Point {
    /**
     * X coordinate of this point
     */
    private final float x;
    /**
     * Y coordinate of this point
     */
    private final float y;
    /**
     * Time of creation (may be uninitialized at 0)
     */
    private final long timestamp;

    /**
     * Creates a point with a timestamp of 0
     * @param x - x of new point
     * @param y - y of new point
     */
    public Point(float x, float y){
        this.x = x;
        this.y = y;
        this.timestamp = 0;
    }

    /**
     * Creates a point with the given coordinates and timestamp
     * @param x - x of new point
     * @param y - y of new point
     * @param timestamp - timestamp of new point
     */
    public Point(float x, float y, long timestamp){
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    /**
     * Gets the X coordinate of this point.
     * @return - x coordinate
     */
    public float getX(){
        return x;
    }

    /**
     * Gets the Y coordinate of this point.
     * @return - y coordinate
     */
    public float getY(){
        return y;
    }

    /**
     * Finds the midpoint between two given points
     * @param pt1 - First point
     * @param pt2 - Second point
     * @return - The midpoint between the two input points.
     */
    public static Point midPointOf(Point pt1, Point pt2){
        return new Point((pt1.getX() + pt2.getX())/2, (pt1.getY() + pt2.getY())/2);
    }

    /**
     * Returns a point between the two given, at the fraction distance from the first.
     * @param pt1 - First point
     * @param pt2 - Second point
     * @param fraction - Fraction of distance from the first point to the second point.
     * @return - Point result of linear interpolation between the two input points.
     */
    public static Point linearInterp(Point pt1, Point pt2, float fraction){
        float diffX = pt2.getX() - pt1.getX();
        float diffY = pt2.getY() - pt1.getY();

        return new Point(pt1.getX() + fraction*diffX, pt1.getY() + fraction*diffY);
    }

    /**
     * Distance to another point.
     * @param other - Point to measure distance to.
     * @return - Distance from this point to the input point.
     */
    public float distanceTo(Point other){
        return (float) Math.sqrt(Math.pow(this.getX() - other.getX(),2)
                + Math.pow(this.getY() - other.getY(),2));
    }

    /**
     * Finds velocity straight from another point based on distance and timestamps.
     * @param start - Other point to calculate velocity from.
     * @return - The velocity from the other point to this one.
     */
    public float velocityFrom(Point start){
        return distanceTo(start)/(this.timestamp - start.timestamp);
    }
}
