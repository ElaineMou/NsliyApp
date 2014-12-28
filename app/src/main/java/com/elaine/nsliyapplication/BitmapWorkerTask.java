package com.elaine.nsliyapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Elaine on 12/28/2014.
 */
public class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private File file;
    private int reqWidth;
    private int reqHeight;

    public BitmapWorkerTask(ImageView imageView, int width, int height){
        // ensure garbage collection with weak reference
        imageViewReference = new WeakReference<ImageView>(imageView);
        reqWidth = width;
        reqHeight = height;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        file = params[0];
        return decodeThumbnail(file);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
        if(isCancelled()){
            bitmap = null;
        }

        if(imageViewReference!=null && bitmap !=null){
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if(this == bitmapWorkerTask && imageView != null){
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public static boolean cancelPotentialWork(File file, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = BitmapWorkerTask.getBitmapWorkerTask(imageView);

        if(bitmapWorkerTask!=null){
            final File bitmapFile = bitmapWorkerTask.getFile();
            // if bitmapFile is not yet set or is different
            if(bitmapFile == null || bitmapFile != file){
                bitmapWorkerTask.cancel(true);
            } else {
                // Same work already in progress
                return false;
            }
        }
        // No task associated with the Imageview or existing task was cancelled
        return true;
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView){
        if(imageView!=null){
            final Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable){
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private Bitmap decodeThumbnail(File imageFile){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(),options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        int inSampleSize = 1;

        // Find greatest inSampleSize where image is just larger than bounds
        if(imageHeight > reqHeight || imageWidth > reqWidth){
            final int halfHeight = imageHeight/2;
            final int halfWidth = imageWidth/2;

            while( (halfHeight / inSampleSize ) > reqHeight && (halfWidth/ inSampleSize) > reqWidth){
                inSampleSize *= 2;
            }

        }

        int width;
        int height;

        float scaleX = (float) reqWidth/imageWidth;
        float scaleY = (float) reqHeight/imageHeight;

        if(scaleX < scaleY){
            width = reqWidth;
            height = (int) (((float) imageHeight / imageWidth) * width);
        } else {
            height = reqHeight;
            width = (int) (((float) imageWidth/imageHeight) * height);
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap sampleBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),options);
        return Bitmap.createScaledBitmap(sampleBitmap,width,height, false);
    }

    public File getFile(){
        return file;
    }
}
