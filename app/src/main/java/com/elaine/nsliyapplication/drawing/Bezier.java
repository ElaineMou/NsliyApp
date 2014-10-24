package com.elaine.nsliyapplication.drawing;

import android.graphics.Path;

import java.util.ArrayList;

/**
 * Created by Elaine on 10/24/2014.
 */
public class Bezier extends Path {

    public static final int SEGMENT_DISTANCE = 2;

    public Bezier(Point prev, Point current){
        this.moveTo(prev.getX(),prev.getY());
        this.lineTo(current.getX(),current.getY());
    }

    public Bezier(Point prev2, Point prev1,Point current){
        Point midPt1 = Point.midPointOf(prev2,prev1);
        Point midPt2 = Point.midPointOf(prev1,current);

        float distance = midPt1.distanceTo(midPt2);
        int numberOfSegments = (int) Math.min(128, Math.max(Math.floor(distance / SEGMENT_DISTANCE), 32));

        this.moveTo(midPt1.getX(),midPt1.getY());

        float t = 0.0f;
        for (int i=0;i<numberOfSegments;i++){
            t = ((float)i)/numberOfSegments;
            float tt = t*t;
            float u = 1-t;
            float uu = u*u;

            float x = uu*midPt1.getX();
            x += 2*u*t*prev1.getX();
            x += tt*midPt2.getX();

            float y = uu*midPt1.getY();
            y += 2*u*t*prev1.getY();
            y += tt*midPt2.getY();

            this.lineTo(x,y);
        }
    }
}
