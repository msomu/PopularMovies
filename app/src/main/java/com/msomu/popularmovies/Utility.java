package com.msomu.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

/**
 * Created by msomu on 10/04/16.
 */
public class Utility {

    private static final String POPULAR_MOVIES_QUERY = "popularity.desc";
    private static final String TOP_RATED_MOVIES_QUERY = "vote_average.desc";

    public static boolean isMostPopular(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_popular))
                .equals(context.getString(R.string.pref_sort_popular));
    }

    public static boolean isFav(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_fav))
                .equals(context.getString(R.string.pref_sort_fav));
    }

    public static String getSortPreference(Context context) {
        if (isMostPopular(context)) {
            return POPULAR_MOVIES_QUERY;
        } else {
            return TOP_RATED_MOVIES_QUERY;
        }
    }

    public static String getSortValue(Context context) {
        if (isMostPopular(context)) {
            return context.getString(R.string.pref_sort_popular);
        } else if (isFav(context)) {
            return context.getString(R.string.pref_sort_fav);
        } else {
            return context.getString(R.string.pref_sort_high_rated);
        }
    }


    public static void renderImage(final Context context, final String originalWebUrl, final ImageView imageView) {
//        //check the table for original url
//        Cursor query = context.getContentResolver().query(MoviesContract.ImageEntry.CONTENT_URI, new String[]{MoviesContract.ImageEntry.COLUMN_IMAGE}, MoviesContract.ImageEntry.COLUMN_URL + "== ?", new String[]{originalWebUrl}, null);
//        //if not present
//        DatabaseUtils.dumpCursor(query);
//        if (query.moveToFirst() && query.getBlob(0) != null) {
//            Log.d("Image","From cache");
//            imageView.setImageBitmap(getImage(query.getBlob(0)));
//        } else {
//            Log.d("Image","From web");
        // load the picture from web
//            Picasso.with(imageView.getContext()).load(originalWebUrl).into(imageView, new Callback() {
//                @Override
//                public void onSuccess() {
//                    // save the picture in db
//                    ContentValues contentValues = new ContentValues();
//                    contentValues.put(MoviesContract.ImageEntry.COLUMN_URL,originalWebUrl);
//                    Bitmap bmap = Bitmap.createBitmap(imageView.getDrawingCache());
//                    contentValues.put(MoviesContract.ImageEntry.COLUMN_IMAGE,getBytes(bmap));
//                    context.getContentResolver().insert(MoviesContract.ImageEntry.CONTENT_URI,contentValues);
//                    imageView.setDrawingCacheEnabled(false);
//                }
//
//                @Override
//                public void onError() {
//
//                }
//            });
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // Bitmap is loaded, use image here
                imageView.setImageBitmap(bitmap);
//                    ContentValues contentValues = new ContentValues();
//                    contentValues.put(MoviesContract.ImageEntry.COLUMN_URL,originalWebUrl);
//                    Bitmap bmap = Bitmap.createBitmap(bitmap);
//                    contentValues.put(MoviesContract.ImageEntry.COLUMN_IMAGE,getBytes(bmap));
//                    context.getContentResolver().insert(MoviesContract.ImageEntry.CONTENT_URI,contentValues);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Picasso.with(context).load(originalWebUrl).into(target);
    }


    //if present
    //load the picture form db
    // }


    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap != null) {
            Log.d("Image", "saved");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        } else {
            return null;
        }
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
