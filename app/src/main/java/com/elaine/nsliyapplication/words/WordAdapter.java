package com.elaine.nsliyapplication.words;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.elaine.nsliyapplication.CreateWordActivity;
import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.view.AsyncDrawable;
import com.elaine.nsliyapplication.view.BitmapLruCache;
import com.elaine.nsliyapplication.view.BitmapWorkerTask;
import com.elaine.nsliyapplication.view.DiskLruImageCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Displays words in a customized ListView format.
 * Created by Elaine on 1/11/2015.
 */
public class WordAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    File[] wordFiles;

    private final float scale;
    public static final int ITEM_WIDTH = 60;
    public static Bitmap placeHolderBitmap;
    private final int scaledItemWidth;
    private DiskLruImageCache diskCache;
    private BitmapLruCache memoryCache;

    public WordAdapter(Context context, File[] wordFiles, BitmapLruCache lruCache,
                       DiskLruImageCache diskLruImageCache){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.wordFiles = wordFiles;

        scale = context.getResources().getDisplayMetrics().density;
        scaledItemWidth = (int) (ITEM_WIDTH*scale);

        placeHolderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sandglass);
        this.memoryCache = lruCache;
        this.diskCache = diskLruImageCache;
    }

    @Override
    public int getCount() {
        return wordFiles.length;
    }

    @Override
    public Object getItem(int position) {
        return wordFiles[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            view = inflater.inflate(R.layout.view_word,parent,false);
            holder = new ViewHolder();

            holder.pronunciations = (LinearLayout) view.findViewById(R.id.pronunciations);
            holder.thumbnails = (LinearLayout) view.findViewById(R.id.thumbnails);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        ArrayList<File> imageFiles = null;
        try {
            imageFiles = getImageFilesFromWordFile(wordFiles[position]);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(imageFiles!=null) {
            for(File imageFile: imageFiles) {
                FrameLayout imageFrame = new FrameLayout(context);
                imageFrame.setLayoutParams(new GridView.LayoutParams(scaledItemWidth, scaledItemWidth));
                imageFrame.setForegroundGravity(Gravity.CENTER);
                imageFrame.setBackgroundColor(Color.LTGRAY);

                ImageView imageView = new ImageView(context);
                loadBitmap(imageFile, imageView);
                imageFrame.addView(imageView);
                holder.thumbnails.addView(imageFrame);
            }
        }

        return view;
    }

    public void loadBitmap(File file, ImageView imageView) {
        final String imageKey = file.getParentFile().getName();
        final Bitmap bitmap = memoryCache.get(imageKey);
        if(bitmap!=null && !bitmap.isRecycled()){
            imageView.setImageBitmap(bitmap);
        } else {
            if(BitmapWorkerTask.cancelPotentialWork(file, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView,
                        scaledItemWidth, scaledItemWidth, memoryCache, diskCache);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(),
                        placeHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(file);
            }
        }

    }

    ArrayList<File> getImageFilesFromWordFile(File wordFile) throws JSONException {
        ArrayList<File> list = new ArrayList<File>();

        if(wordFile.exists()) {
            BufferedReader bufferedReader = null;
            StringBuilder stringBuilder = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(wordFile));
                stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();

                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (stringBuilder != null) {
                File picturesDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                JSONArray jsonArray = jsonObject.getJSONArray(CreateWordActivity.JSON_KEY_CHARACTERS);
                int length = jsonArray.length();
                for(int i=0;i<length;i++) {
                    String characterFolder = jsonArray.getString(i);
                    Log.v("WordAdapter","CharacterFolder: " + characterFolder + " " + i);
                    File directory = new File(picturesDirectory,characterFolder);
                    if(directory!=null) {
                        list.add(new File(directory, DrawView.DISPLAY_IMAGE_NAME));
                    }
                }
            }
        }
        return list;
    }

    private class ViewHolder{
        public LinearLayout thumbnails;
        public LinearLayout pronunciations;
    }
}
