package com.futureelectronics.futuremaplecandy;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.futureelectronics.futuremaplecandy.scan.BleScanner;
import com.futureelectronics.futuremaplecandy.scan.BleScannerProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service implements BLEConnection.OnConnectionClosedListener{

	// Flag to check the bound status
	public boolean mBound = false;

	private final static String TAG = BluetoothLeService.class.getSimpleName();

	public static final int BT_OK = 0;
	public static final int BT_NO_MANANGER = -1;
	public static final int BT_NO_ADAPTER = -2;
	public static final int BT_DISABLED = -3;

	private BluetoothManager mBluetoothManager;
	public BluetoothAdapter mBluetoothAdapter;

	protected LocalBroadcastManager mBroadcastManager;

	public Map<String, BLEConnection> mConnections = new HashMap<>();

	public final static String ACTION_FINISHED_SCAN =
			"com.futureelectronics.futuremaplecandy.ACTION_FINISHED_SCAN";
	public final static String ACTION_DEVICE_FOUND =
			"com.futureelectronics.futuremaplecandy.ACTION_DEVICE_FOUND";
	public final static String ACTION_SERVICE_BOUND =
			"com.futureelectronics.futuremaplecandy.ACTION_SERVICE_BOUND";
	public final static String ACTION_SERVICE_READY =
			"com.futureelectronics.futuremaplecandy.ACTION_SERVICE_READY";

	private Handler mHandler = new Handler();

    private BleScanner mScanner;

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
        mBroadcastManager.sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, Bundle mBundle) {
		final Intent intent = new Intent(action);
		intent.putExtras(mBundle);
        mBroadcastManager.sendBroadcast(intent);
	}


	public class LocalBinder extends Binder {
		BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(mBroadcastManager == null) {
            mBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        }
        if(!isInitialized()){
            initialize();
        }
        return START_NOT_STICKY;
    }

	@Override
	public IBinder onBind(Intent intent) {
		mBound = true;
		if(mBroadcastManager == null) {
			mBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
		}
        if(!isInitialized()){
            initialize();
        }
		broadcastUpdate(ACTION_SERVICE_BOUND);
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mBound = false;
		// After using a given device, you should make sure that BluetoothGatt.close() is called
		// such that resources are cleaned up properly.  In this particular example, close() is
		// invoked when the UI is disconnected from the Service.
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	public int initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
                AppLog.e(TAG, "Unable to initialize BluetoothManager.");
				return BT_NO_MANANGER;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			AppLog.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return BT_NO_ADAPTER;
		}
		else{
			/**
			 * Ensures Blue tooth is enabled on the device. If Blue tooth is not
			 * currently enabled, fire an intent to display a dialog asking the
			 * user to grant permission to enable it.
			 */
			if (!mBluetoothAdapter.isEnabled()) {
				return BT_DISABLED;
			}
		}

		broadcastUpdate(ACTION_SERVICE_READY);

		return BT_OK;
	}

	/**
	 * Check if this service has been initialized
	 * @return true if initialized, false otherwise
	 */
	public boolean isInitialized(){
		if(mBluetoothManager != null && mBluetoothAdapter != null){
			return true;
		}

		return false;
	}

	private Runnable mLeScanStopper = new Runnable() {
		@Override
		public void run() {
			stopLeScan();
		}
	};

	public void startLeScan(BleScanner.BatchDevicesListener scanListener, final long scanPeriodMs)
	{
        if (mScanner == null) {
            mScanner = BleScannerProvider.getBLEScanner(this);
        }

        mScanner.setBatchDevicesListener(scanListener);
        mScanner.startBleScan();

		// Stops scanning after a pre-defined scan period.
		mHandler.postDelayed(mLeScanStopper, scanPeriodMs);
	}

	public void stopLeScan()
	{
        if (mScanner != null) {
            mScanner.stopBleScan();
            mScanner.setBatchDevicesListener(null);
        }
		mHandler.removeCallbacks(mLeScanStopper);
		broadcastUpdate(ACTION_FINISHED_SCAN);
	}

    public boolean isScanning() {
        if (mScanner == null) {
            return false;
        }
        return mScanner.isScanning();
    }

	public BLEConnection createConnection(BluetoothDevice device){
		BLEConnection bleConnection = new BLEConnection(getApplicationContext(), device, this);
		mConnections.put(device.getAddress(), bleConnection);
		return bleConnection;
	}

	public void onConnectionClosed(BLEConnection connection){
		mConnections.remove(connection.getDevice().getAddress());
	}

	/**
	 * Adding the necessary Intent filters for Broadcast receivers
	 *
	 * @return {@link IntentFilter}
	 */
	public static IntentFilter makeBTUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_FINISHED_SCAN);
		intentFilter.addAction(BluetoothLeService.ACTION_DEVICE_FOUND);
		intentFilter.addAction(BluetoothLeService.ACTION_SERVICE_BOUND);
		intentFilter.addAction(BluetoothLeService.ACTION_SERVICE_READY);
		return intentFilter;
	}

	public BLEConnection getDeviceConnection(String devAddress){
		return mConnections.get(devAddress);
	}

	public void stopService(){
        disconnectAll();
        stopSelf();
    }

    public void disconnectAll()
    {
//        Iterator it = mConnections.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            BLEConnection conn = (BLEConnection)pair.getValue();
//            conn.close();
//        }
        for(BLEConnection conn : mConnections.values()){
            conn.disconnect();
        }
    }
}
