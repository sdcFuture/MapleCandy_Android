package com.futureelectronics.futuremaplecandy;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Kyle.Harman on 10/5/2017.
 */

public class Utils {

    public static String bytesToString(byte[] data){
        String res = "";
        if(data != null){
            for(int i=0; i<data.length; i++){
                res += String.format("%02x", data[i]);
            }
        }

        return res;
    }

    public static int bytesTo16bitUintLE(byte[] b, int off){
        return ((int)b[off]&0x000000FF) | (((int)b[off+1]<<8)&0x0000FF00);
    }

    public static int bytesTo16bitIntLE(byte[] b, int off){
        int temp = ((int)b[off]&0x000000FF) | (((int)b[off+1]<<8)&0x0000FF00);
        if((temp & 0x8000) != 0){
            temp |= ~(0x0000FFFF);
        }

        return temp;
    }

    public static int bytesTo32bitIntLE(byte[] b, int off){
        return ((int)b[off]&0x000000FF) | (((int)b[off+1]<<8)&0x0000FF00) | (((int)b[off+2]<<16)&0x00FF0000) | (((int)b[off+3]<<24)&0xFF000000);
    }

    public static int bytesTo16bitIntBE(byte[] b, int off){
        return ((int)b[off+1]&0x000000FF) | (((int)b[off]<<8)&0x0000FF00);
    }

    public static int bytesTo32bitIntBE(byte[] b, int off){
        return ((int)b[off+3]&0x000000FF) | (((int)b[off+2]<<8)&0x0000FF00) | (((int)b[off+1]<<16)&0x00FF0000) | (((int)b[off]<<24)&0xFF000000);
    }

    public static int int16bitToBytesLE(byte[] b, int off, int i){
        b[off] = (byte)(i&0x000000FF);
        b[off+1] = (byte)((i>>8)&0x000000FF);

        return off+2;
    }

    public static byte[] int16bitToBytesLE(int i){
        byte[] b = new byte[2];
        int16bitToBytesLE(b, 0, i);

        return b;
    }

    public static int int32bitToBytesLE(byte[] b, int off, int i){
        b[off] = (byte)(i&0x000000FF);
        b[off+1] = (byte)((i>>8)&0x000000FF);
        b[off+2] = (byte)((i>>16)&0x000000FF);
        b[off+3] = (byte)((i>>24)&0x000000FF);

        return off+4;
    }

    public static byte[] int32bitToBytesLE(int i){
        byte[] b = new byte[4];
        int32bitToBytesLE(b, 0, i);

        return b;
    }

    public static int int16bitToBytesBE(byte[] b, int off, int i){
        b[off+1] = (byte)(i&0x000000FF);
        b[off] = (byte)((i>>8)&0x000000FF);

        return off+2;
    }

    public static byte[] int16bitToBytesBE(int i){
        byte[] b = new byte[2];
        int16bitToBytesBE(b, 0, i);

        return b;
    }

    public static int int32bitToBytesBE(byte[] b, int off, int i){
        b[off+3] = (byte)(i&0x000000FF);
        b[off+2] = (byte)((i>>8)&0x000000FF);
        b[off+1] = (byte)((i>>16)&0x000000FF);
        b[off] = (byte)((i>>24)&0x000000FF);

        return off+4;
    }

    public static byte[] int32bitToBytesBE(int i){
        byte[] b = new byte[4];
        int32bitToBytesBE(b, 0, i);

        return b;
    }

    /**
     * This method converts dp unit to equivalent device specific value in pixels.
     *
     * @param dp      A value in dp(Device independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return An integer value to represent Pixels equivalent to dp according to device
     */
    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * (metrics.densityDpi / 160f));
        return px;
    }
}
