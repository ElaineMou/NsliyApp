package com.elaine.nsliyapplication.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/** Used to immediately load placeholder bitmaps before real one loads asynchronously.
 * Created by Elaine on 12/28/2014.
 */
public class AsyncDrawable extends BitmapDrawable {

    /**
     * Used to ensure garbage collection of task.
     */
    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskWeakReference;

    public AsyncDrawable(Resources resources, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask){
        super(resources,bitmap);
        bitmapWorkerTaskWeakReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
    }

    public BitmapWorkerTask getBitmapWorkerTask(){
        return bitmapWorkerTaskWeakReference.get();
    }
}
