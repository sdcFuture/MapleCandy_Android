package com.futureelectronics.futuremaplecandy.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kyle.Harman on 2/25/2016.
 */
public abstract class BleScanner {
    private final Map<String, ScannedDevice> mScannedDevs;
    private ArrayList<ScannedDevice> mScannedDevsArr;
    private Context mContext;
    private Handler mHandler;
    private ScheduledExecutorService mScheduler;
    private BatchDevicesListener mBatchDevicesListener;
    private DeviceListener mDeviceListener;

    public static final int SCAN_MODE_BALANCED = 0;
    public static final int SCAN_MODE_LOW_LATENCY = 1;
    public static final int SCAN_MODE_LOW_POWER = 2;

    protected BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;

    public BleScanner(Context context){
        mContext = context;
        mScannedDevs = new HashMap<>();
        mScannedDevsArr = new ArrayList<>();
        mHandler = new Handler();

        mBluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }

//    protected void onDeviceDetected(BluetoothDevice device, int rssi, byte[] scanRecord, long timestamp){
//        mHandler.post(new DevAdder(device, rssi, scanRecord, timestamp));
//    }

//    private class DevAdder implements Runnable{
//        private final BluetoothDevice device;
//        private final int rssi;
//        private final byte[] scanRecord;
//        private final long timestamp;
//
//        public DevAdder(BluetoothDevice device, int rssi, byte[] scanRecord, long timestamp){
//            this.device = device;
//            this.rssi = rssi;
//            this.scanRecord = scanRecord;
//            this.timestamp = timestamp;
//        }
//
//        @Override
//        public void run() {
//            ScannedDevice scannedDev = mScannedDevs.get(device.getAddress());
//            if(scannedDev == null){
//                scannedDev = new ScannedDevice(device);
//                mScannedDevs.put(device.getAddress(), scannedDev);
//                mScannedDevsArr.add(scannedDev);
//            }
//            scannedDev.updateInfo(rssi, scanRecord, timestamp);
//        }
//    }

    public interface BatchDevicesListener{
        void onDevicesUpdated(List<ScannedDevice> scannedDevices);
    }

    public interface DeviceListener {
        void onDeviceUpdated(ScannedDevice scannedDevice);
    }

    public void setBatchDevicesListener(BatchDevicesListener listener){
        mBatchDevicesListener = listener;
    }

    public void setDeviceListener(DeviceListener listener){
        mDeviceListener = listener;
    }

    private Runnable mStartBatchReport = new Runnable() {
        @Override
        public void run() {
            mHandler.post(mReportBatchResults);
        }
    };

    private Runnable mReportBatchResults = new Runnable() {
        @Override
        public void run() {
            if(mBatchDevicesListener != null){
                synchronized (mScannedDevs) {
                    mBatchDevicesListener.onDevicesUpdated(mScannedDevsArr);
                }
            }
        }
    };

    private void startBatchReporter(long period){
        mScheduler = Executors.newSingleThreadScheduledExecutor();
        mScheduler.scheduleAtFixedRate(mStartBatchReport, 200, period, TimeUnit.MILLISECONDS);
    }

    private void stopBatchReporter(){
        if(mScheduler != null){
            mScheduler.shutdown();
            mScheduler = null;
//            mHandler.postDelayed(mReportBatchResults, 1000);
        }
    }

    abstract protected void startScanInternal();
    abstract protected void stopScanInternal();
    abstract public void setScanMode(int scanMode);

    public boolean startBleScan(long reportInterval){
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled() || mScanning){
            return false;
        }

        synchronized (mScannedDevs){
            mScannedDevs.clear();
            mScannedDevsArr.clear();
        }

        mScanning = true;
        startScanInternal();
        startBatchReporter(reportInterval);

        return true;
    }

    public boolean startBleScan(){
        return startBleScan(500);
    }

    public void stopBleScan(){
        if(mScanning){
            mScanning = false;
            stopScanInternal();
            stopBatchReporter();
        }
    }

    public boolean isScanning(){
        return mScanning;
    }

    protected void onDeviceDiscovered(BluetoothDevice btDev, int rssi, byte[] scanRecord, long timestampNanos) {
        synchronized (mScannedDevs){
            ScannedDevice scannedDev = mScannedDevs.get(btDev.getAddress());
            if(scannedDev == null){
                scannedDev = new ScannedDevice(btDev);
                mScannedDevs.put(btDev.getAddress(), scannedDev);
                mScannedDevsArr.add(scannedDev);
            }
            scannedDev.updateInfo(rssi, scanRecord, timestampNanos);
            if(mDeviceListener != null){
                mDeviceListener.onDeviceUpdated(scannedDev);
            }
        }
    }
}
