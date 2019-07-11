package com.futureelectronics.futuremaplecandy;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by Kyle Harman on 12/29/2016.
 */

public class AppLog {

    public static void v(String tag, String msg){
//        FirebaseCrash.logcat(Log.VERBOSE, tag, msg);
        Log.v(tag, msg);
        FirebaseCrash.log("V/"+tag+": "+msg);
    }

    public static void d(String tag, String msg){
//        FirebaseCrash.logcat(Log.DEBUG, tag, msg);
        Log.d(tag, msg);
        FirebaseCrash.log("D/"+tag+": "+msg);
    }

    public static void i(String tag, String msg){
//        FirebaseCrash.logcat(Log.INFO, tag, msg);
        Log.i(tag, msg);
        FirebaseCrash.log("I/"+tag+": "+msg);
    }

    public static void w(String tag, String msg){
//        FirebaseCrash.logcat(Log.WARN, tag, msg);
        Log.w(tag, msg);
        FirebaseCrash.log("W/"+tag+": "+msg);
    }

    public static void e(String tag, String msg){
//        FirebaseCrash.logcat(Log.ERROR, tag, msg);
        Log.e(tag, msg);
        FirebaseCrash.log("E/"+tag+": "+msg);
    }


}
