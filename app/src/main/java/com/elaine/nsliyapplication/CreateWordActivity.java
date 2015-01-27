package com.elaine.nsliyapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.input.Pronunciation;
import com.elaine.nsliyapplication.input.SyllableEntryView;
import com.elaine.nsliyapplication.view.BitmapLruCache;
import com.elaine.nsliyapplication.view.DiskLruImageCache;
import com.elaine.nsliyapplication.view.ImageAdapter;
import com.elaine.nsliyapplication.view.RetainViewFragment;
import com.elaine.nsliyapplication.words.BuildWordView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Allows user to create words by tapping on existing characters.
 * Created by Elaine on 1/8/2015.
 */
public class CreateWordActivity extends BaseActivity {

    /**
     * String to be shown when a word has no known pronunciations.
     */
    public static final String DEFAULT_UNKNOWN_STRING = "????";
    public static final String WORD_PREFIX = "word";
    public static final String JSON_KEY_CHARACTERS = "characters";
    public static final String JSON_KEY_PRONUNCIATIONS = "pronunciations";
    public static final String JSON_KEY_MEANING = "meaning";
    public static final String TEXT_FILE_TYPE = ".txt";
    /**
     * Memory cache to be used by this activity
     */
    private BitmapLruCache memoryCache;
    /**
     * Disk cache for images for this activity.
     */
    private DiskLruImageCache diskCache = new DiskLruImageCache();
    /**
     * List of files for characters used in the building word.
     */
    private ArrayList<String> keys = new ArrayList<String>();
    /**
     * List of pronunciations to save to file.
     */
    private ArrayList<Pronunciation> savedPronunciations = new ArrayList<Pronunciation>();
    /**
     * Disk cache size (in bytes)
     */
    private static final int DISK_CACHE_SIZE = 1024*1024*10;
    /**
     * Subdirectory name for the disk cache
     */
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        super.onCreateDrawer();

        // Recover memory cache if previous instance existed
        RetainViewFragment retainViewFragment = RetainViewFragment
                .findOrCreateRetainFragment(getFragmentManager());
        memoryCache = retainViewFragment.memoryCache;
        // If no cache saved, make a new one using 1/8 maximum runtime memory (in KB)
        if(memoryCache == null){
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory/8;
            memoryCache = new BitmapLruCache(cacheSize);
            retainViewFragment.memoryCache = memoryCache;
        }
        // Initialize new disk cache for activity
        File cacheDir = getDiskCacheDir(this, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);

        scale = getResources().getDisplayMetrics().density;
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
    public void onResume(){
        super.onResume();
        GridView gridView = (GridView) findViewById(R.id.view_grid);

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(dir!=null) {
            // Get character directories
            File[] filesList = dir.listFiles();
            ArrayList<File> files = new ArrayList<File>();
            for (File file : filesList) {
                if (file.isDirectory()) {
                    File bitmapFile = new File(file, DrawView.DISPLAY_IMAGE_NAME);
                    if (bitmapFile.exists()) {
                        files.add(bitmapFile);
                    }
                }
            }

            TextView textView = (TextView) findViewById(R.id.empty_message);
            // If none, display empty message
            textView.setVisibility(View.INVISIBLE);

            // Make a new adapter of these files and the current memory and disk caches
            ImageAdapter imageAdapter = new ImageAdapter(this, files, memoryCache, diskCache);
            gridView.setAdapter(imageAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    addCharacter(((ImageView) ((FrameLayout)view).getChildAt(0)).getDrawable(),
                            ((ImageAdapter) parent.getAdapter()).getFiles().get(position)
                                    .getParentFile());
                }
                });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.draw, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_clear) {
            ((LinearLayout)findViewById(R.id.thumbnail_series)).removeAllViews();
            ((LinearLayout)findViewById(R.id.pronunciation_series)).removeAllViews();
            keys.clear();
            savedPronunciations.clear();
            return true;
        } else if (id == R.id.action_undo){
            LinearLayout linearLayout = ((LinearLayout)findViewById(R.id.thumbnail_series));
            linearLayout.removeViewAt(linearLayout.getChildCount() - 1);
            linearLayout = ((LinearLayout)findViewById(R.id.pronunciation_series));
            linearLayout.removeViewAt(linearLayout.getChildCount() - 1);
            keys.remove(keys.size() - 1);
            savedPronunciations.remove(savedPronunciations.size() - 1);
        } else if (id == R.id.action_save){
            try {
                saveToFile();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private File generateFileName(){
        int n=0;
        Random random = new Random();
        String fileName;
        File file;
        // Guarantee a unique folder for new character
        do {
            n += random.nextInt(100);
            fileName = WORD_PREFIX + n + TEXT_FILE_TYPE ;
            file = new File(getExternalFilesDir(
                    Environment.DIRECTORY_DOCUMENTS), fileName);
        } while (file.exists());

        return file;
    }

    private void saveToFile() throws JSONException, IOException {
        if(savedPronunciations!=null) {
            File file = generateFileName();

            JSONObject jsonObject = new JSONObject();
            JSONArray characterNames = new JSONArray();
            for (String key : keys) {
                characterNames.put(key);
            }
            JSONArray pronunciations = new JSONArray();
            for (Pronunciation pronunciation : savedPronunciations) {
                if (pronunciation != null) {
                    pronunciations.put(pronunciation.syllable + SyllableEntryView.SYLLABLE_TONE_SEPARATOR
                            + pronunciation.tone);
                } else {
                    pronunciations.put(DEFAULT_UNKNOWN_STRING + SyllableEntryView.SYLLABLE_TONE_SEPARATOR
                            + Pronunciation.Tone.UNKNOWN.toString());
                }
            }

            String meaning = ((EditText) findViewById(R.id.meaning_edit_text)).getText().toString();

            jsonObject.put(JSON_KEY_CHARACTERS, characterNames);
            jsonObject.put(JSON_KEY_PRONUNCIATIONS, pronunciations);
            jsonObject.put(JSON_KEY_MEANING, meaning);

            FileWriter fileWriter = new FileWriter(file);
            try {
                fileWriter.write(jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fileWriter.flush();
                fileWriter.close();
            }

            HashSet<String> stringHashSet = new HashSet<String>();
            stringHashSet.addAll(keys);
            SharedPreferences sharedPreferences = getSharedPreferences(DrawActivity.PREFERENCES_FILE_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            for (String string : stringHashSet) {
                int currentValue = sharedPreferences.getInt(string, 0);
                editor.putInt(string, currentValue + 1);
                Log.v("CreateWordActivity", "Put " + string + "," + (currentValue + 1));
            }
            editor.commit();
        }
    }

    public void addCharacter(final Drawable image, final File directory){
        final ArrayList<Pronunciation> possiblePronunciations = Pronunciation.getListFromDirectory(directory);
        int size = possiblePronunciations.size();
        Pronunciation pronunciation = null;
        if(size > 1){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
            builder.setView(dialogView);

            final ArrayList<String> strings = new ArrayList<String>();
            for(Pronunciation possible: possiblePronunciations){
                strings.add(possible.syllable + " " + possible.tone);
            }

            final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line,strings);
            spinner.setAdapter(arrayAdapter);

            builder.setTitle(R.string.select_pronunciation_message)
                    .setPositiveButton(R.string.use, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addCharacterToViews(possiblePronunciations.get
                                    (spinner.getSelectedItemPosition()),image,directory);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Nothing
                        }
                    }
                    );
            builder.create().show();
        } else{
            if (size == 1) {
                pronunciation = possiblePronunciations.get(0);
            }
            addCharacterToViews(pronunciation,image,directory);
        }

    }

    public void addCharacterToViews(Pronunciation pronunciation, Drawable image, File directory){
        TextView textView = new TextView(this);
        Resources resources = getResources();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int)(ViewActivity.VIEW_IMAGE_SIZE*scale - 10), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 5, 5, 5);
        textView.setLayoutParams(params);
        textView.setBackgroundColor(resources.getColor(R.color.pale_green));
        textView.setTextColor(resources.getColor(R.color.dark_green));

        if(pronunciation!=null) {
            textView.setText(pronunciation.syllable + " " + pronunciation.tone);
        } else {
            textView.setText(DEFAULT_UNKNOWN_STRING);
        }
        ((LinearLayout) findViewById(R.id.pronunciation_series)).addView(textView);

        keys.add(directory.getName());
        savedPronunciations.add(pronunciation);
        ((BuildWordView) findViewById(R.id.build_word_view)).addThumbnail(image);
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

    public DiskLruImageCache getDiskCache(){
        return diskCache;
    }

}
