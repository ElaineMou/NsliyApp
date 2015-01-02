package com.elaine.nsliyapplication.view;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Memory cache for String keys to bitmaps for quick retrieval.
 * Created by Elaine on 12/28/2014.
 */
public class BitmapLruCache extends LruCache<String,Bitmap> {
    /**
     * @param maxSize The maximum sum of the sizes of the entries in this cache - in KB.
     */
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    /**
     * Gets the size of the bitmap in kilobytes.
     * @param key - String used to access bitmap.
     * @param bitmap - Bitmap to be sized.
     * @return - Size of the image in KB.
     */
    @Override
    protected int sizeOf(String key, Bitmap bitmap){
        // Cache size in kilobytes, not number of items
        return bitmap.getByteCount() / 1024;
    }
}
