package com.elaine.nsliyapplication.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.ViewCharActivity;

import java.io.File;
import java.util.ArrayList;

/** Fills a GridView with imageViews centered in cell.
 * Created by Elaine on 12/27/2014.
 */
public class ImageAdapter extends BaseAdapter {

    /**
     * The context for this adapter.
     */
    private Context context;
    /**
     * Resources to pull values from.
     */
    private Resources resources;
    /**
     * Files to display characters from.
     */
    private ArrayList<File> files;
    /**
     * Bitmap used when image is still loading.
     */
    public static Bitmap placeHolderBitmap;
    /**
     * Density scale for image loading.
     */
    private final float scale;
    /**
     * Disk cache for images.
     */
    private DiskLruImageCache diskCache;
    /**
     * In-memory cache for images.
     */
    private BitmapLruCache memoryCache;
    /**
     * Color value for the image background.
     */
    private int backgroundColorId;

    public ImageAdapter(Context context, ArrayList<File> files, BitmapLruCache lruCache,
                        DiskLruImageCache diskLruImageCache){
        this.context = context;
        this.resources = context.getResources();
        this.files = files;

        if(placeHolderBitmap==null){
            placeHolderBitmap = BitmapFactory.
                    decodeResource(resources, R.drawable.sandglass);
        }
        scale = resources.getDisplayMetrics().density;
        this.memoryCache = lruCache;
        this.diskCache = diskLruImageCache;
        backgroundColorId = resources.getColor(R.color.g50);
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        FrameLayout imageFrame;
        FrameLayout imageBorder;

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            // Create dark green border
            imageBorder = new FrameLayout(context);
            int frameWidth = (int) resources.getDimension(R.dimen.char_thumb_padding);
            imageBorder.setPadding(frameWidth,frameWidth,frameWidth,frameWidth);
            int frameSize = (int) (ViewCharActivity.VIEW_IMAGE_SIZE*scale);
            imageBorder.setLayoutParams(new GridView.LayoutParams(frameSize, frameSize));
            imageBorder.setBackgroundColor(resources.getColor(R.color.g800));

            // Create light green background
            imageFrame = new FrameLayout(context);
            imageFrame.setForegroundGravity(Gravity.CENTER);
            imageFrame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageFrame.setBackgroundColor(backgroundColorId);

            imageBorder.addView(imageFrame);
        } else {
            // Retrieve old border and background and clear the character from it.
            imageBorder = (FrameLayout) convertView;
            imageFrame = (FrameLayout) imageBorder.getChildAt(0);
            imageFrame.removeAllViews();
        }
        // Load character into the background.
        ImageView imageView = new ImageView(context);
        loadBitmap(files.get(position), imageView);
        imageFrame.addView(imageView);

        return imageBorder;
    }

    /**
     * Load imageView with thumbnail-size character using an AsyncTask.
     * @param file
     * @param imageView
     */
    public void loadBitmap(File file, ImageView imageView) {
        final String imageKey = file.getParentFile().getName();
        final Bitmap bitmap = memoryCache.get(imageKey);
        if(bitmap!=null && !bitmap.isRecycled()){
            imageView.setImageBitmap(bitmap);
        } else {
            if(BitmapWorkerTask.cancelPotentialWork(file, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView,
                        (int) (scale * ViewCharActivity.VIEW_IMAGE_SIZE),
                        (int) (scale * ViewCharActivity.VIEW_IMAGE_SIZE), memoryCache, diskCache);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(resources,
                        placeHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(file);
            }
        }

    }

    /**
     * Remove the file from the given position, file system, and the memory and disk caches.
     * @param position
     * @return - If the file was successfully deleted from the file system.
     */
    public boolean remove(int position){
        // Get character directory
        File directory = files.get(position).getParentFile();
        String key = directory.getName();
        // Remove character from cache
        diskCache.remove(key);
        memoryCache.remove(key).recycle();

        // Delete character directory from files and in-memory list
        files.remove(position);
        notifyDataSetChanged();

        File[] fileList = directory.listFiles();
        for(File file:fileList){
            file.delete();
        }

        return directory.delete();
    }

    /**
     * Returns the list of files associated with the adapter.
     * @return - The files used for the adapter.
     */
    public ArrayList<File> getFiles(){
        return files;
    }

    /**
     * If the file list is empty.
     * @return - If the file list for this adapter is empty.
     */
    public boolean filesEmpty(){
        return files.isEmpty();
    }
}
