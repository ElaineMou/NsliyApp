package com.elaine.nsliyapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.view.BitmapLruCache;
import com.elaine.nsliyapplication.view.DiskLruImageCache;
import com.elaine.nsliyapplication.view.ImageAdapter;
import com.elaine.nsliyapplication.view.RetainViewFragment;

import java.io.File;
import java.util.ArrayList;

/**
 * Displays thumbnails of all characters on file.
 * Created by Elaine on 12/27/2014.
 */
public class ViewCharActivity extends DrawerActivity {

    /**
     * Size (in dp) of image thumbnails (square)
     */
    public static final int VIEW_IMAGE_SIZE = 90;
    public static final int EDIT_CHAR_REQUEST = 1;

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
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
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
            if(files.isEmpty()){
                textView.setVisibility(View.VISIBLE);
            } else { // Otherwise, add all files of display images in the list
                textView.setVisibility(View.INVISIBLE);

                // Make a new adapter of these files and the current memory and disk caches
                ImageAdapter imageAdapter = new ImageAdapter(this, files, memoryCache, diskCache);
                gridView.setAdapter(imageAdapter);
                // Set listener to delete items on long clicks.
                gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position,
                                                   long id) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewCharActivity.this);
                        builder.setTitle(R.string.delete_character_dialog);

                        SharedPreferences sharedPreferences = getSharedPreferences(
                                DrawActivity.PREFERENCES_FILE_KEY,MODE_PRIVATE);
                        String key = ((ImageAdapter)parent.getAdapter()).getFiles().get(position).getParentFile().getName();
                        int uses = sharedPreferences.getInt(key,0);
                        Log.v("ViewActivity","Got " + key + "," + uses );
                        if( uses == 1){
                            builder.setMessage("One word contains this character.");
                        } else if (uses > 1){
                            builder.setMessage(uses + " words contain this character.");
                        }

                        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (((ImageAdapter) parent.getAdapter()).remove(position)) {
                                    // Notify user through short Toast
                                    Toast.makeText(ViewCharActivity.this, R.string.deleted_toast,
                                            Toast.LENGTH_SHORT).show();
                                    // Display empty message when all files are gone
                                    if (((ImageAdapter) parent.getAdapter()).filesEmpty()) {
                                        findViewById(R.id.empty_message).setVisibility(View.VISIBLE);
                                    }
                                }
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
                        return true;
                    }
                });
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Open editing activity on the relevant file
                        Intent intent = new Intent(ViewCharActivity.this,EditDrawActivity.class);
                        intent.putExtra(EditDrawActivity.FILE_EXTRA_NAME, ((ImageAdapter) parent.
                                getAdapter()).getFiles().get(position).getParentFile().getAbsolutePath());
                        startActivityForResult(intent, EDIT_CHAR_REQUEST);
                    }
                });
            }
        }
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode==RESULT_OK && requestCode == EDIT_CHAR_REQUEST){
            String directory = data.getStringExtra(EditDrawActivity.DIRECTORY_RETURN_EXTRA);
            memoryCache.remove(directory);
            diskCache.remove(directory);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent intent = new Intent(this,DrawActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes the disk cache off the main thread.
     */
    class InitDiskCacheTask extends AsyncTask<File,Void,Void>{
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
