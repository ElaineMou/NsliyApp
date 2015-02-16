package com.elaine.nsliyapplication.words;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elaine.nsliyapplication.CreateWordActivity;
import com.elaine.nsliyapplication.DrawActivity;
import com.elaine.nsliyapplication.R;
import com.elaine.nsliyapplication.ReviewWordActivity;
import com.elaine.nsliyapplication.ViewWordActivity;
import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.Pronunciation;
import com.elaine.nsliyapplication.input.SyllableEntryView;
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
import java.util.HashSet;

/**
 * Displays words in a customized ListView format.
 * Created by Elaine on 1/11/2015.
 */
public class WordAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<File> wordFiles;

    private final float scale;
    public static final int ITEM_WIDTH = 60;
    public static Bitmap placeHolderBitmap;
    private final int scaledItemWidth;
    private DiskLruImageCache diskCache;
    private BitmapLruCache memoryCache;
    private int bgImageColorId;

    public WordAdapter(Context context, File[] wordFiles, BitmapLruCache lruCache,
                       DiskLruImageCache diskLruImageCache){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.wordFiles = new ArrayList<File>();

        for(File wordFile: wordFiles){
            this.wordFiles.add(wordFile);
        }

        scale = context.getResources().getDisplayMetrics().density;
        scaledItemWidth = (int) (ITEM_WIDTH*scale);

        placeHolderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sandglass);
        this.memoryCache = lruCache;
        this.diskCache = diskLruImageCache;

        bgImageColorId = context.getResources().getColor(R.color.cream);
    }

    @Override
    public int getCount() {
        return wordFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return wordFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            view = inflater.inflate(R.layout.view_word,parent,false);
            holder = new ViewHolder();

            holder.pronunciations = (LinearLayout) view.findViewById(R.id.pronunciations);
            holder.thumbnails = (LinearLayout) view.findViewById(R.id.thumbnails);
            holder.reviewButton = (Button) view.findViewById(R.id.review_word_button);
            holder.deleteButton = (Button) view.findViewById(R.id.delete_word_button);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWord(position);
            }
        });
        holder.reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewWord(position);
            }
        });

        holder.pronunciations.removeAllViews();
        holder.thumbnails.removeAllViews();

        ArrayList<File> imageFiles = null;
        try {
            imageFiles = getImageFilesFromWordFile(wordFiles.get(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(imageFiles!=null) {
            for(File imageFile: imageFiles) {
                FrameLayout imageFrame = new FrameLayout(context);
                imageFrame.setLayoutParams(new GridView.LayoutParams(scaledItemWidth, scaledItemWidth));
                imageFrame.setForegroundGravity(Gravity.CENTER);
                imageFrame.setBackgroundColor(bgImageColorId);

                ImageView imageView = new ImageView(context);
                loadBitmap(imageFile, imageView);
                imageFrame.addView(imageView);
                holder.thumbnails.addView(imageFrame);
            }
        }

        ArrayList<Pronunciation> pronunciations = null;
        try{
            pronunciations = getPronunciationsFromWordFile(wordFiles.get(position));
        } catch (JSONException e){
            e.printStackTrace();
        }

        if(pronunciations!=null){
            for(Pronunciation pronunciation: pronunciations){
                TextView textView = new TextView(context);
                Resources resources = context.getResources();

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(scaledItemWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                textView.setLayoutParams(params);
                textView.setBackgroundColor(resources.getColor(R.color.pale_green));
                textView.setTextColor(resources.getColor(R.color.dark_green));
                textView.setGravity(Gravity.CENTER);
                textView.setText(pronunciation.syllable + " " + pronunciation.tone);
                holder.pronunciations.addView(textView);
            }
        }

        return view;
    }

    private void reviewWord(int position) {
        File file = wordFiles.get(position);
        Intent intent = new Intent(context, ReviewWordActivity.class);
        intent.putExtra(ReviewWordActivity.EXTRAS_KEY_WORD_FILE,file);
        context.startActivity(intent);
    }

    private void deleteWord(int position) {
        HashSet<String> stringHashSet = null;
        try {
            stringHashSet = getUniqueCharsInWord(wordFiles.get(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(wordFiles.get(position).delete()){
            Toast.makeText(context, R.string.deleted_word_toast,Toast.LENGTH_SHORT).show();

            if(stringHashSet!=null) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(DrawActivity.PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (String string : stringHashSet) {
                    int currentValue = sharedPreferences.getInt(string, 1);
                    editor.putInt(string, currentValue - 1);;
                }
                editor.commit();
            }

            wordFiles.remove(position);
            // Display empty message when all files are gone
            if(wordFiles.isEmpty()){
                if(context instanceof ViewWordActivity) {
                    ((ViewWordActivity)context).findViewById(R.id.empty_message).setVisibility(View.VISIBLE);
                }
            }
            notifyDataSetChanged();
        }
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

    HashSet<String> getUniqueCharsInWord(File wordFile) throws JSONException{
        HashSet<String> set = new HashSet<String>();

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
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                JSONArray jsonArray = jsonObject.getJSONArray(CreateWordActivity.JSON_KEY_CHARACTERS);
                int length = jsonArray.length();
                for(int i=0;i<length;i++) {
                    String characterFolder = jsonArray.getString(i);
                    set.add(characterFolder);
                }
            }
        }
        return set;
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
                    File directory = new File(picturesDirectory,characterFolder);
                    if(directory!=null) {
                        list.add(new File(directory, DrawView.DISPLAY_IMAGE_NAME));
                    }
                }
            }
        }
        return list;
    }

    ArrayList<Pronunciation> getPronunciationsFromWordFile(File pronunciationFile) throws JSONException {
        ArrayList<Pronunciation> list = new ArrayList<Pronunciation>();

        if(pronunciationFile.exists()) {
            BufferedReader bufferedReader = null;
            StringBuilder stringBuilder = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(pronunciationFile));
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
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                JSONArray jsonArray = jsonObject.getJSONArray(CreateWordActivity.JSON_KEY_PRONUNCIATIONS);
                int length = jsonArray.length();
                for(int i=0;i<length;i++) {
                    String pronunciation = jsonArray.getString(i);
                    String[] syllableTone = pronunciation.split(SyllableEntryView.SYLLABLE_TONE_SEPARATOR);
                    if(syllableTone.length == 2) {
                        Pronunciation.Tone tone = Pronunciation.Tone.UNKNOWN;
                        for (Pronunciation.Tone value : Pronunciation.Tone.values()) {
                            if (value.toString().equals(syllableTone[1])) {
                                tone = value;
                            }
                        }
                        list.add(new Pronunciation(syllableTone[0], tone));
                    }
                }
            }
        }
        return list;
    }

    private class ViewHolder{
        public LinearLayout thumbnails;
        public LinearLayout pronunciations;
        public Button reviewButton;
        public Button deleteButton;
    }
}
