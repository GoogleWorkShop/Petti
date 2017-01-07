package com.firebase.petti.petti.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by yahav on 1/6/2017.
 */

public class ImageLoaderUtils {

    public static ImageLoader imageLoader;
//    public static ImageLoader dogRegistrationImageLoader;
//    public static ImageLoader ownerRegistrationImageLoader;

//    private static DisplayImageOptions matchesOptions;

    public static void initImageLoader(Context context) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);

        initImageLoader();

    }

    private static void initImageLoader() {
        imageLoader = ImageLoader.getInstance();

    }

    public static boolean setImage(String photoUrl, ImageView imageView) {
        if (imageView != null) {
            imageLoader.displayImage(photoUrl, imageView);
            return true;
        } else {
            // IMAGE VIEW IS NULL - NEED TO PRINT MESSAGE
            return false;
        }
    }

    public static boolean setImage(String photoUrl, ImageView imageView, int defaultPic) {
        DisplayImageOptions matchesOptions = new DisplayImageOptions.Builder()
                .showImageOnFail(defaultPic)
                .showImageForEmptyUri(defaultPic)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        if (imageView != null) {
            imageLoader.displayImage(photoUrl, imageView, matchesOptions);
            return true;
        } else {
            // IMAGE VIEW IS NULL - NEED TO PRINT MESSAGE
            return false;
        }
    }
}
