package com.elaine.nsliyapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.ViewActivity;

import java.io.File;
import java.util.ArrayList;

/** Fills a GridView with imageViews centered in cell.
 * Created by Elaine on 12/27/2014.
 */
public class ImageAdapter extends BaseAdapter {

    Context context;
    ArrayList<File> files;
    public static Bitmap placeHolderBitmap;
    final float scale;
    DiskLruImageCache diskCache;
    BitmapLruCache memoryCache;

    public ImageAdapter(Context context, ArrayList<File> files, BitmapLruCache lruCache,
                        DiskLruImageCache diskLruImageCache){
        this.context = context;
        this.files = files;

        if(placeHolderBitmap==null){
            placeHolderBitmap = BitmapFactory.
                    decodeResource(context.getResources(), R.drawable.sandglass);
        }
        scale = context.getResources().getDisplayMetrics().density;
        this.memoryCache = lruCache;
        this.diskCache = diskLruImageCache;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        FrameLayout imageFrame;

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageFrame = new FrameLayout(context);
            int frameSize = (int) (ViewActivity.VIEW_IMAGE_SIZE*scale);
            imageFrame.setLayoutParams(new GridView.LayoutParams(frameSize,frameSize));
            imageFrame.setForegroundGravity(Gravity.CENTER);
            imageFrame.setBackgroundColor(Color.LTGRAY);
        } else {
            imageFrame = (FrameLayout) convertView;
        }
        ImageView imageView = new ImageView(context);
        loadBitmap(files.get(position), imageView);
        imageFrame.addView(imageView);

        return imageFrame;
    }

    public void loadBitmap(File file, ImageView imageView) {
        final String imageKey = file.getParentFile().getName();
        final Bitmap bitmap = memoryCache.get(imageKey);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
        } else {
            if(BitmapWorkerTask.cancelPotentialWork(file, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView,
                        (int) (scale * ViewActivity.VIEW_IMAGE_SIZE),
                        (int) (scale * ViewActivity.VIEW_IMAGE_SIZE), memoryCache, diskCache);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(),
                        placeHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(file);
            }
        }

    }

}
