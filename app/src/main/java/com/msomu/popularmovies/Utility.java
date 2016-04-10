package com.msomu.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    public static String getSortPreference(Context context) {
        if(isMostPopular(context)){
            return POPULAR_MOVIES_QUERY;
        }else{
            return TOP_RATED_MOVIES_QUERY;
        }
    }
}
