package com.futureelectronics.futuremaplecandy.scan;

import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import java.util.List;

/**
 * Created by Kyle Harman on 1/11/2017.
 */

@TargetApi(21)
public class LollipopBleScanner extends BleScanner {
    private ScanCallback mScanCallback;
    private static final byte[] EMPTY_SCAN_RECORD = new byte[0];

//    private int scanMode = ScanSettings.SCAN_MODE_BALANCED;
    private int scanMode = ScanSettings.SCAN_MODE_LOW_LATENCY;

    public LollipopBleScanner(Context context){
        super(context);

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                ScanRecord record = result.getScanRecord();
                byte[] scanRecBytes = (record == null || record.getBytes() == null) ? EMPTY_SCAN_RECORD : record.getBytes();

                LollipopBleScanner.this.onDeviceDiscovered(result.getDevice(), result.getRssi(), scanRecBytes, result.getTimestampNanos());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    public void setScanMode(int scanMode){
        if(scanMode == ScanSettings.SCAN_MODE_LOW_POWER || scanMode == ScanSettings.SCAN_MODE_BALANCED || scanMode == ScanSettings.SCAN_MODE_LOW_LATENCY){
            this.scanMode = scanMode;
        }
    }

    protected void startScanInternal(){
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
//        scanner.startScan(mScanCallback);

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        settingsBuilder.setReportDelay(0);
        settingsBuilder.setScanMode(scanMode);
        scanner.startScan(null, settingsBuilder.build(), mScanCallback);
    }

    protected void stopScanInternal(){
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
    }
}
