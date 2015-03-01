package com.elaine.nsliyapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.elaine.nsliyapplication.input.Pronunciation;
import com.elaine.nsliyapplication.input.SyllableEntryView;
import com.elaine.nsliyapplication.view.BitmapLruCache;
import com.elaine.nsliyapplication.view.DiskLruImageCache;
import com.elaine.nsliyapplication.words.ReviewAdapter;
import com.elaine.nsliyapplication.words.ReviewCharView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Activity allows review of words by step-by-step process.
 * Created by Elaine on 1/14/2015.
 */
public class ReviewWordActivity extends Activity {

    /**
     * Memory cache to be used by this activity
     */
    private BitmapLruCache memoryCache;
    /**
     * Disk cache for images for this activity.
     */
    private DiskLruImageCache diskCache = new DiskLruImageCache();
    /**
     * Disk cache size (in bytes)
     */
    private static final int DISK_CACHE_SIZE = 1024*1024*10;
    /**
     * Subdirectory name for the disk cache
     */
    private static final String DISK_CACHE_SUBDIR = "reviewStrokes";

    /**
     * Extras key for file to review.
     */
    public static final String EXTRAS_KEY_WORD_FILE = "wordFile";

    private File wordFile = null;
    ArrayList<File> characterFolders = new ArrayList<File>();
    ArrayList<Pronunciation> pronunciations = new ArrayList<Pronunciation>();
    String meaning;

    private int currentChar = 0;

    private ReviewAdapter reviewAdapter = null;
    private GridView gridView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_word);
        ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.g700)));
        }

        wordFile = (File) getIntent().getSerializableExtra(EXTRAS_KEY_WORD_FILE);
        loadFromFile();

        // TODO: USE CACHING TO FIX MEMORY ISSUES
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory/8;
        memoryCache = new BitmapLruCache(cacheSize);;
        // Initialize new disk cache for activity
        File cacheDir = getDiskCacheDir(this, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);

        Button button = (Button) findViewById(R.id.back_back);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = v.getBackground();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        drawable.setColorFilter(new LightingColorFilter(Color.LTGRAY, 1));
                        drawable.invalidateSelf();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Rect r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (!r.contains((int) event.getX(), (int) event.getY())) {
                            drawable.clearColorFilter();
                            drawable.invalidateSelf();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        drawable.clearColorFilter();
                        drawable.invalidateSelf();
                        r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (r.contains((int) event.getX(), (int) event.getY())) {
                            rewind();
                        }
                        break;
                }
                return true;
            }
        });

        button = (Button) findViewById(R.id.back);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = v.getBackground();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        drawable.setColorFilter(new LightingColorFilter(Color.LTGRAY, 1));
                        drawable.invalidateSelf();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Rect r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (!r.contains((int) event.getX(), (int) event.getY())) {
                            drawable.clearColorFilter();
                            drawable.invalidateSelf();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        drawable.clearColorFilter();
                        drawable.invalidateSelf();
                        r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (r.contains((int) event.getX(), (int) event.getY())) {
                            previous();
                        }
                        break;
                }
                return true;
            }
        });

        button = (Button) findViewById(R.id.forward);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = v.getBackground();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        drawable.setColorFilter(new LightingColorFilter(Color.LTGRAY, 1));
                        drawable.invalidateSelf();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Rect r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (!r.contains((int) event.getX(), (int) event.getY())) {
                            drawable.clearColorFilter();
                            drawable.invalidateSelf();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        drawable.clearColorFilter();
                        drawable.invalidateSelf();
                        r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (r.contains((int) event.getX(), (int) event.getY())) {
                            next();
                        }
                        break;
                }
                return true;
            }
        });

        button = (Button) findViewById(R.id.forward_forward);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = v.getBackground();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        drawable.setColorFilter(new LightingColorFilter(Color.LTGRAY, 1));
                        drawable.invalidateSelf();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Rect r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (!r.contains((int) event.getX(), (int) event.getY())) {
                            drawable.clearColorFilter();
                            drawable.invalidateSelf();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        drawable.clearColorFilter();
                        drawable.invalidateSelf();
                        r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (r.contains((int) event.getX(), (int) event.getY())) {
                            fastForward();
                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Initializes the disk cache off the main thread.
     */
    class InitDiskCacheTask extends AsyncTask<File,Void,Void> {
        @Override
        protected Void doInBackground(File... params){
            synchronized(diskCache.mDiskCacheLock){
                File cacheDir = params[0];
                // Opens the disk cache in the given directory
                diskCache.open(cacheDir, DISK_CACHE_SIZE);
                // Notifies waiting objects once ready
                diskCache.mDiskCacheStarting = false;
                diskCache.mDiskCacheLock.notifyAll();
            }
            return null;
        }
    }

    /**
     * Return directory for the disk cache
     * @param context - Context to be used
     * @param uniqueName - Unique name for the file directory
     * @return - The directory of the disk cache
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = "";
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable()){
            // Use external cache directory if possible
            File externalCache = context.getExternalCacheDir();
            if(externalCache!=null){
                cachePath = externalCache.getPath();
            }
        } else {
            // Otherwise use this context's cache directory
            File cache = context.getCacheDir();
            if(cache != null){
                cachePath = cache.getPath();
            }
        }
        // Return a new directory of the cache path plus the unique name provided
        return new File(cachePath + File.separator + uniqueName);
    }

    @Override
    protected void onDestroy(){
        if(diskCache!=null){
            diskCache.clearCache();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(gridView==null) {
            gridView = (GridView) findViewById(R.id.chars_grid);
            reviewAdapter = new ReviewAdapter(this, characterFolders, pronunciations);
            gridView.setAdapter(reviewAdapter);
        }
    }

    private void rewind(){
        boolean cleared = getReviewer(currentChar).clear();
        if(!cleared && currentChar > 0){
            currentChar--;
            getReviewer(currentChar).clear();
        }
    }

    private void previous(){
        boolean removed = getReviewer(currentChar).removeStroke();
        if(!removed && currentChar > 0){
            currentChar--;
            getReviewer(currentChar).removeStroke();
        }
    }

    private void next(){
        boolean added = getReviewer(currentChar).addStroke();
        if(!added && currentChar < reviewAdapter.getHolders().size() - 1){
            currentChar++;
            getReviewer(currentChar).addStroke();
        }
    }

    private void fastForward(){
        boolean filled = getReviewer(currentChar).drawChar();
        if(!filled && currentChar < reviewAdapter.getHolders().size() - 1){
            currentChar++;
            getReviewer(currentChar).drawChar();
        }
    }

    private ReviewCharView getReviewer(int position){
        return reviewAdapter.getHolders().get(position).reviewCharView;
    }

    protected void loadFromFile(){
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
                File picturesDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                JSONObject jsonObject = null;
                JSONArray jsonChars = null;
                JSONArray jsonPronunciations = null;
                String jsonMeaning = null;

                try {
                    jsonObject = new JSONObject(stringBuilder.toString());
                    jsonChars = jsonObject.getJSONArray(CreateWordActivity.JSON_KEY_CHARACTERS);
                    jsonPronunciations = jsonObject.getJSONArray(CreateWordActivity.JSON_KEY_PRONUNCIATIONS);
                    jsonMeaning = jsonObject.getString(CreateWordActivity.JSON_KEY_MEANING);
                } catch (JSONException e){
                    e.printStackTrace();
                }
                if(jsonChars !=null) {
                    int length = jsonChars.length();
                    for (int i = 0; i < length; i++) {
                        String characterFolder = "";
                        try {
                            characterFolder = jsonChars.getString(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        File directory = new File(picturesDirectory, characterFolder);
                        if (directory != null && directory.exists()) {
                            characterFolders.add(directory);
                        } else {
                            characterFolders.add(null);
                        }
                    }
                }
                if(jsonPronunciations !=null){
                    int length = jsonPronunciations.length();
                    for(int i=0;i<length;i++) {
                        String pronunciation = "";
                        try {
                            pronunciation = jsonPronunciations.getString(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String[] syllableTone = pronunciation.split(SyllableEntryView.SYLLABLE_TONE_SEPARATOR);
                        if(syllableTone.length == 2) {
                            Pronunciation.Tone tone = Pronunciation.Tone.UNKNOWN;
                            for (Pronunciation.Tone value : Pronunciation.Tone.values()) {
                                if (value.toString().equals(syllableTone[1])) {
                                    tone = value;
                                }
                            }
                            pronunciations.add(new Pronunciation(syllableTone[0], tone));
                        }
                    }
                }
                if(jsonMeaning !=null && !jsonMeaning.isEmpty()){
                    meaning = jsonMeaning;
                    TextView textView = (TextView) findViewById(R.id.meaning_text_view);
                    textView.setText(meaning);
                    textView.setMovementMethod(new ScrollingMovementMethod());
                }
            }
        }
    }
}
