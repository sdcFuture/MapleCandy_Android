package com.futureelectronics.futuremaplecandy.scan;

import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.SystemClock;

/**
 * Created by Kyle Harman on 1/11/2017.
 */

public class JBBleScanner extends BleScanner implements LeScanCallback {
    public JBBleScanner(Context context){
        super(context);
    }

    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        onDeviceDiscovered(device, rssi, scanRecord, SystemClock.elapsedRealtimeNanos());
    }

    public void setScanMode(int scanMode){
    }

    protected void startScanInternal(){
        if(mBluetoothAdapter != null){
            mBluetoothAdapter.startLeScan(this);
        }
    }

    protected void stopScanInternal(){
        if(mBluetoothAdapter != null){
            mBluetoothAdapter.stopLeScan(this);
        }
    }
}
