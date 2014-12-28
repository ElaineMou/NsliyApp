package com.elaine.nsliyapplication;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Elaine on 12/28/2014.
 */
public class AsyncDrawable extends BitmapDrawable {

    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskWeakReference;

    public AsyncDrawable(Resources resources, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask){
        super(resources,bitmap);
        bitmapWorkerTaskWeakReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
    }

    public BitmapWorkerTask getBitmapWorkerTask(){
        return bitmapWorkerTaskWeakReference.get();
    }
}
