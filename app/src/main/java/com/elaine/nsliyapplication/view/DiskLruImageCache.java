package com.elaine.nsliyapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.elaine.nsliyapplication.BuildConfig;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Written by Platonius on StackOverflow, modified by Elaine Mou
 */
public class DiskLruImageCache {
    /**
     * The disk cache used to implement this cache.
     */
    private DiskLruCache mDiskCache;
    /**
     * Chosen image compression format.
     */
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.PNG;
    /**
     * Compression quality.
     */
    private int mCompressQuality = 90;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    /**
     * Tag used for logging.
     */
    private static final String TAG = "DiskLruImageCache";

    /**
     * Lock used for synchronization of cache.
     */
    public final Object mDiskCacheLock = new Object();
    /**
     * True until everything is initialized.
     */
    public boolean mDiskCacheStarting = true;

    public DiskLruImageCache() {}

    /**
     * Open disk cache using directory given.
     * @param cacheDir - Location of disk cache
     * @param diskCacheSize - Maximum size of cache
     */
    public void open(File cacheDir, int diskCacheSize){
        try {
            mDiskCache = DiskLruCache.open( cacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeBitmapToFile( Bitmap bitmap, DiskLruCache.Editor editor )
            throws IOException, FileNotFoundException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream( 0 ), Utils.IO_BUFFER_SIZE );
            return bitmap.compress( mCompressFormat, mCompressQuality, out );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    /**
     * Places a bitmap in the cache.
     * @param key - Key used to access image
     * @param data - Image to be added
     */
    public void put( String key, Bitmap data ) {

        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( key );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {
                mDiskCache.flush();
                editor.commit();
                if ( BuildConfig.DEBUG ) {
                    Log.d( "cache_test_DISK_", "image put on disk cache " + key );
                }
            } else {
                editor.abort();
                if ( BuildConfig.DEBUG ) {
                    Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
                }
            }
        } catch (IOException e) {
            if ( BuildConfig.DEBUG ) {
                Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    /**
     * Retrieves bitmap from cache
     * @param key - Key used to retrieve bitmap
     * @return - Image returned from cache
     */
    public Bitmap getBitmap( String key ) {

        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn =
                        new BufferedInputStream( in, Utils.IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
        }

        return bitmap;

    }

    /**
     * If the cache contains the given key.
     * @param key - Key to be checked
     * @return - True if cache has the string, false if not
     */
    public boolean containsKey( String key ) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    /**
     * Clears the disk cache.
     */
    public void clearCache() {
        if ( BuildConfig.DEBUG ) {
            Log.d("cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Removes bitmap from cache.
     * @param key - Key to be removed.
     * @return - true if removal is successful, false if not
     */
    public boolean remove(String key){
        try{
            mDiskCache.remove(key);
            return true;
        } catch (IOException e) {
            Log.d("DiskLruImageCache","Exception when removing " + key + " from cache");
        }
        return false;
    }

    /**
     * Returns the cache's directory folder
     * @return - Directory folder of the cache
     */
    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }

}
