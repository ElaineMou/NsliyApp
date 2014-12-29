package com.elaine.nsliyapplication.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/** Asynchronous task to retrieve bitmap from file, memory cache, or disk cache.
 * Created by Elaine on 12/28/2014.
 */
public class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private File file;
    private int reqWidth;
    private int reqHeight;
    private BitmapLruCache memoryCache;
    private DiskLruImageCache diskCache;

    public BitmapWorkerTask(ImageView imageView, int width, int height, BitmapLruCache cache,
                            DiskLruImageCache diskLruImageCache){
        // ensure garbage collection with weak reference
        imageViewReference = new WeakReference<ImageView>(imageView);
        reqWidth = width;
        reqHeight = height;
        memoryCache = cache;
        diskCache = diskLruImageCache;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        file = params[0];
        String key = file.getParentFile().getName();
        // Check disk cache for image
        Bitmap thumbnail = getBitmapFromDiskCache(key);
        if(thumbnail == null){
            thumbnail = decodeThumbnail(file);
        }
        addBitmapToCaches(key, thumbnail);
        return thumbnail;
    }

    private void addBitmapToCaches(String key, Bitmap bitmap) {
        if(memoryCache.get(key) == null){
            memoryCache.put(key, bitmap);
        }

        synchronized (diskCache.mDiskCacheLock){
            if(diskCache != null && diskCache.getBitmap(key) == null){
                diskCache.put(key, bitmap);
            }
        }
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

    public Bitmap getBitmapFromDiskCache(String key){
        synchronized(diskCache.mDiskCacheLock){
            while(diskCache.mDiskCacheStarting){
                try{
                    diskCache.mDiskCacheLock.wait();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            if(diskCache != null){
                return diskCache.getBitmap(key);
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
