package com.futureelectronics.futuremaplecandy.scan;

import android.content.Context;
import android.os.Build;

import com.futureelectronics.futuremaplecandy.AppLog;

/**
 * Created by Kyle Harman on 1/11/2017.
 */

public class BleScannerProvider {
    private final static String TAG = BleScannerProvider.class.getSimpleName();

    public static BleScanner getBLEScanner(Context context){
        if (Build.VERSION.SDK_INT < 21){
            AppLog.d(TAG, "Using JB BLE scanner");
            return new JBBleScanner(context);
        }
        else{
            AppLog.d(TAG, "Using Lollipop BLE scanner");
            // Should return Lollipop scanner
//            return new LollipopBleScanner(context);
            return new JBBleScanner(context);
        }
    }
}
