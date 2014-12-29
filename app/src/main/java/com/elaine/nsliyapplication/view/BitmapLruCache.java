package com.elaine.nsliyapplication.view;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Memory cache for String keys to bitmaps for quick retrieval.
 * Created by Elaine on 12/28/2014.
 */
public class BitmapLruCache extends LruCache<String,Bitmap> {
    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap){
        // Cache size in kilobytes, not number of items
        return bitmap.getByteCount() / 1024;
    }
}
