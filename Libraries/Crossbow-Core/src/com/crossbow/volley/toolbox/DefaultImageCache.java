package com.crossbow.volley.toolbox;

import android.content.ComponentCallbacks2;
import android.graphics.Bitmap;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;

import com.crossbow.volley.CrossbowImageCache;

/*
 * Copyright (C) 2014 Patrick Doyle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Image cache used for the Image loader. Uses the {@link android.support.v4.util.LruCache} for storage.
 * This also supports trimming of memory from the {@link android.content.ComponentCallbacks2#onTrimMemory(int)} callback to
 * release bitmaps when the system runs low on memory.
 */
public class DefaultImageCache implements CrossbowImageCache {

    private LruCache<String, Bitmap> imageCache;

    /**
     * @param size - number of byes to use for the cache;
     */
    public DefaultImageCache(int size) {
        imageCache = new LruCache<String, Bitmap>(size){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return BitmapCompat.getAllocationByteCount(value);
            }
        };
    }

    @Override
    public synchronized Bitmap getBitmap(String url) {
        return imageCache.get(url);
    }

    @Override
    public synchronized void putBitmap(String url, Bitmap bitmap) {
        imageCache.put(url, bitmap);
    }

    @Override
    public void trimMemory(int level) {

        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                trimCache();
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                emptyCache();
                break;
        }
    }

    @Override
    public void onLowMemory() {
        emptyCache();
    }

    private synchronized void trimCache() {
        imageCache.trimToSize(imageCache.maxSize() / 2);
    }

    private synchronized void emptyCache() {
        imageCache.evictAll();
    }
}