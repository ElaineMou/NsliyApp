package com.elaine.nsliyapplication.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/** Asynchronous task to retrieve bitmap from file in proper size and save to caches.
 * Created by Elaine on 12/28/2014.
 */
public class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {

    /**
     * Reference to ensure garbage collection of the ImageView.
     */
    private final WeakReference<ImageView> imageViewReference;
    /**
     * File to retrieve image from.
     */
    private File file;
    /**
     * Width bounds of final image to be displayed.
     */
    private int reqWidth;
    /**
     * Height bounds of final image to be displayed.
     */
    private int reqHeight;
    /**
     * Memory cache to save image to for later retrieval.
     */
    private BitmapLruCache memoryCache;
    /**
     * Disk cache to save image to to save image to for later retrieval.
     */
    private DiskLruImageCache diskCache;

    public BitmapWorkerTask(ImageView imageView, int width, int height, BitmapLruCache cache,
                            DiskLruImageCache diskLruImageCache){
        // ensure garbage collection with weak reference
        imageViewReference = new WeakReference<ImageView>(imageView);
        reqWidth = width;
        reqHeight = height;
        memoryCache = cache;
        diskCache = diskLruImageCache;

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("TAG","Uncaught Exception:",paramThrowable);
            }
        });
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        file = params[0];
        String key = file.getParentFile().getName();
        // Check disk cache for image
        Bitmap thumbnail = getBitmapFromDiskCache(key);
        // If not in disk cache, retrieve from memory cache
        if(thumbnail == null){
            thumbnail = memoryCache.get(key);
        }
        // If not in either cache, pull from file.
        if(thumbnail == null){
            thumbnail = decodeThumbnail(file);
        }
        addBitmapToCaches(key, thumbnail);
        return thumbnail;
    }

    /**
     * Add bitmap to memory and disk cache as long as each does not already contain it.
     * @param key - Key to add to caches with.
     * @param bitmap - Image to be saved.
     */
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

        // Ensure this is the task associated with the ImageView
        if(imageViewReference!=null && bitmap !=null){
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if(this == bitmapWorkerTask && imageView != null){
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * Cancel in-progress work from other tasks linked to same ImageView.
     * @param file - File the View should be sourcing from.
     * @param imageView - ImageView to be written to.
     * @return - If the current task was cancelled or did not exist; false if work was the same
     */
    public static boolean cancelPotentialWork(File file, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = BitmapWorkerTask.getBitmapWorkerTask(imageView);
        // If there is a task associated with the ImageView
        if(bitmapWorkerTask!=null){
            final File bitmapFile = bitmapWorkerTask.getFile();
            // If the file with that task is not yet set or is different, cancel it
            if(bitmapFile == null || bitmapFile != file){
                bitmapWorkerTask.cancel(true);
            } else {
                // Means same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView or existing task was cancelled
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

    /**
     * Retrieve image from the disk cache.
     * @param key - Key to be used to access cache.
     * @return - The bitmap image returned from the cache.
     */
    public Bitmap getBitmapFromDiskCache(String key){
        synchronized(diskCache.mDiskCacheLock){
            // Do not access cache while it is being initialized
            while(diskCache.mDiskCacheStarting){
                try{
                    diskCache.mDiskCacheLock.wait();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            // Return bitmap from cache
            if(diskCache != null){
                return diskCache.getBitmap(key);
            }
        }
        return null;
    }

    /**
     * Retrieves Bitmap from file into appropriately-sized thumbnail
     * @param imageFile - File to source from
     * @return - Resized bitmap that fits bounds of the created task
     */
    private Bitmap decodeThumbnail(File imageFile){
        // Retrieve just image's size before loading actual image data
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

        // If x coordinates need to be shrunk more than y coordinates
        if(scaleX < scaleY){
            // Match width to bounds and scale height accordingly
            width = reqWidth;
            height = (int) (((float) imageHeight / imageWidth) * width);
        } else { // If y coordinates need to be shrunk more than x coordinates
            // Match height to bounds and scale width accordingly
            height = reqHeight;
            width = (int) (((float) imageWidth/imageHeight) * height);
        }

        // Create sampled bitmap at smallest size (divided by power of 2) larger than given bounds
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap sampleBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),options);
        // Return scaled bitmap of thumbnail size
        return Bitmap.createScaledBitmap(sampleBitmap,width,height, false);
    }

    /**
     * Gets the file associated with this task.
     * @return - The file associated with this task.
     */
    public File getFile(){
        return file;
    }
}
