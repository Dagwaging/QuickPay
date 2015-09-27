package edu.rose_hulman.quickpay;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by Zane on 9/27/2015.
 */
public class RequestQueueProvider {
    private static RequestQueue instance;
    private static ImageLoader imageLoader;

    public static synchronized RequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = Volley.newRequestQueue(context.getApplicationContext());
        }

        return instance;
    }

    public static ImageLoader getImageLoader(Context context) {
        if (imageLoader == null) {
            imageLoader = new ImageLoader(getInstance(context), new ImageLoader.ImageCache() {
                private final LruCache<String, Bitmap> cache = new LruCache<>(20);

                @Override
                public Bitmap getBitmap(String url) {
                    return cache.get(url);
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    cache.put(url, bitmap);
                }
            });
        }

        return imageLoader;
    }
}
