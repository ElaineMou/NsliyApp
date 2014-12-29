package com.elaine.nsliyapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.elaine.nsliyapplication.input.DrawView;
import com.elaine.nsliyapplication.view.BitmapLruCache;
import com.elaine.nsliyapplication.view.DiskLruImageCache;
import com.elaine.nsliyapplication.view.ImageAdapter;
import com.elaine.nsliyapplication.view.RetainViewFragment;

import java.io.File;
import java.util.ArrayList;

/**
 * TODO: Make sure that deleting an item clears it from cache as well!!!
 * Created by Elaine on 12/27/2014.
 */
public class ViewActivity extends Activity {

    public static final int VIEW_IMAGE_SIZE = 90;

    private BitmapLruCache memoryCache;
    private DiskLruImageCache diskCache = new DiskLruImageCache();
    private static final int DISK_CACHE_SIZE = 1024*1024*10;
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        RetainViewFragment retainViewFragment = RetainViewFragment
                .findOrCreateRetainFragment(getFragmentManager());
        memoryCache = retainViewFragment.memoryCache;
        if(memoryCache == null){
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory/8;
            memoryCache = new BitmapLruCache(cacheSize);
            retainViewFragment.memoryCache = memoryCache;
        }

        File cacheDir = getDiskCacheDir(this, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = "";
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable()){
            File externalCache = context.getExternalCacheDir();
            if(externalCache!=null){
                cachePath = externalCache.getPath();
            }
        } else {
            File cache = context.getCacheDir();
            if(cache != null){
                cachePath = cache.getPath();
            }
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    @Override
    public void onResume(){
        super.onResume();
        GridView gridView = (GridView) findViewById(R.id.view_grid);
        ArrayList<File> files = new ArrayList<File>();

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(dir!=null) {
            File[] filesList = dir.listFiles();
            for (File file : filesList) {
                if (file.isDirectory()) {
                    File bitmapFile = new File(file, DrawView.DISPLAY_IMAGE_NAME);
                    if (bitmapFile.exists()) {
                        files.add(bitmapFile);
                    }
                }
            }

            ImageAdapter imageAdapter = new ImageAdapter(this, files, memoryCache, diskCache);
            gridView.setAdapter(imageAdapter);
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

    class InitDiskCacheTask extends AsyncTask<File,Void,Void>{
        @Override
        protected Void doInBackground(File... params){
            synchronized(diskCache.mDiskCacheLock){
                File cacheDir = params[0];
                diskCache.open(cacheDir, DISK_CACHE_SIZE);
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
