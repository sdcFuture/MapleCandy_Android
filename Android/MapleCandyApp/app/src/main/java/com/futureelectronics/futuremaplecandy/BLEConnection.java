package com.futureelectronics.futuremaplecandy;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Kyle.Harman on 2/9/2016.
 */
public class BLEConnection extends BluetoothGattCallback {
	private final static String TAG = BLEConnection.class.getSimpleName();

    public static final int MTU_DEFAULT = 23;
    public static final int MTU_MAX = 128;
    public static final int MTU_OVERHEAD_BYTES = 3;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_DISCONNECTING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_DISCOVERING = 4;
    public static final int STATE_READY = 5;

    public final static String ACTION_GATT_CONNECTED = "BLEConnection.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = "BLEConnection.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTING = "BLEConnection.ACTION_GATT_DISCONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "BLEConnection.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "BLEConnection.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_DISCOVERING_SERVICES = "BLEConnection.ACTION_GATT_DISCOVERING_SERVICES";
    public final static String ACTION_DATA_AVAILABLE = "BLEConnection.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_WRITTEN = "BLEConnection.ACTION_DATA_WRITTEN";
    public final static String ACTION_DESCRIPTOR_WRITTEN = "BLEConnection.ACTION_DESCRIPTOR_WRITTEN";
    public final static String ACTION_DESCRIPTOR_READ = "BLEConnection.ACTION_DESCRIPTOR_READ";
    public final static String ACTION_READ_RSSI = "BLEConnection.ACTION_READ_RSSI";
    public final static String ACTION_MTU_CHANGED = "BLEConnection.ACTION_MTU_CHANGED";
    public final static String ACTION_GATT_ERROR = "BLEConnection.ACTION_GATT_ERROR";
    public final static String ACTION_GATT_QUEUE_EMPTY = "BLEConnection.ACTION_GATT_QUEUE_EMPTY";

    public final static String EXTRA_DATA = "BLEConnection.EXTRA_DATA";
    public static final String EXTRA_BYTE_VALUE = "BLEConnection.EXTRA_BYTE_VALUE";
    public static final String EXTRA_GATT_CHARACTERISTIC = "BLEConnection.EXTRA_GATT_CHARACTERISTIC";
    public static final String EXTRA_GATT_DESCRIPTOR = "BLEConnection.EXTRA_GATT_DESCRIPTOR";
    public static final String EXTRA_GATT_OP_STATUS = "BLEConnection.EXTRA_GATT_OP_STATUS";
    public static final String EXTRA_BLE_OP_STATUS = "BLEConnection.EXTRA_BLE_OP_STATUS";
    public static final String EXTRA_RSSI = "BLEConnection.EXTRA_RSSI";
    public final static String EXTRA_ERROR = "BLEConnection.EXTRA_ERROR";

	protected final BluetoothDevice mBluetoothDev;
	protected final String mBluetoothDevAddr;
	protected String mDeviceName;
	protected BluetoothGatt mBluetoothGatt;
	protected final Context mContext;
	protected LocalBroadcastManager mBroadcastManager;

	private int mConnectionState = STATE_DISCONNECTED;

	private final Handler mConnectHandler = new Handler();

	private int mMTU = MTU_DEFAULT;

	private ConcurrentLinkedQueue<BleOperation> mBleOps = new ConcurrentLinkedQueue<>();
	private final Handler mBleOpRetryHandler = new Handler();
	private int mBleOpTries = 0;
	private int mBleOpFailures = 0;
    private int mConnTries = 0;

	// Timeout for connecting to the lock
	protected static final int CONNECT_TO_MS = 6000;
	// Timeout for discovering the lock's services
	protected static final int DISCOVER_TO_MS = 6000;

	private static final int MAX_BLEOP_QUEUE_SIZE = 200;
	private static final int MAX_BLEOP_TRIES = 5;

	private static final int OP_WRITE_CHARA = 0;
	private static final int OP_READ_CHARA = 1;
	private static final int OP_SET_NOTIF = 2;
	private static final int OP_READ_RSSI = 3;
	private static final int OP_DISCOVER_SERVICES = 4;
	private static final int OP_SET_MTU = 5;
	private static final int OP_WRITE_DESCRIP = 6;
	private static final int OP_READ_DESCRIP = 7;

	private final OnConnectionClosedListener mOnConnectionClosedListener;

	public BLEConnection(Context context, BluetoothDevice device, OnConnectionClosedListener onConnectionClosedListener){
		mContext = context;
		mBluetoothDev = device;
		mBluetoothDevAddr = device.getAddress();
		mDeviceName = device.getName();
		mOnConnectionClosedListener = onConnectionClosedListener;
		mBroadcastManager = LocalBroadcastManager.getInstance(mContext);
	}

	public interface OnConnectionClosedListener{
		void onConnectionClosed(BLEConnection connection);
	}

	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
		mConnectHandler.removeCallbacksAndMessages(null);
		if (newState == BluetoothProfile.STATE_CONNECTED) {
			mConnectionState = STATE_CONNECTED;
			Log.i(TAG, "Connected to GATT server. Status = " + status);
			// Delay 100ms before discovering services
			mConnectHandler.postDelayed(mLeDiscoveryDelay, 100);
			broadcastAction(ACTION_GATT_CONNECTED);

		} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
			int oldConnState = mConnectionState;
			mConnectionState = STATE_DISCONNECTED;
			clearBLEOperationsQueue();
			mMTU = MTU_DEFAULT;
            AppLog.i(TAG, "Disconnected from GATT server. Status = "+status);
            if(status == 0x85){
                refreshDeviceCache();
            }
            disconnected();
            if((status == 0x85 || oldConnState == STATE_DISCOVERING || oldConnState == STATE_CONNECTING) && mConnTries++ < 3){
                mConnectHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mConnTries == 2) {
                            connect(true);
                        }else{
                            connect();
                        }
                    }
                }, 50);
            }
            else {
                broadcastAction(ACTION_GATT_DISCONNECTED);
            }
		}
	}

	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		mConnectHandler.removeCallbacksAndMessages(null);
		if (status == BluetoothGatt.GATT_SUCCESS) {
            AppLog.d(TAG, "onServicesDiscovered success");
			mConnectionState = STATE_READY;
			bleOperationDone(OP_DISCOVER_SERVICES);
			broadcastAction(ACTION_GATT_SERVICES_DISCOVERED);
		} else {
            AppLog.w(TAG, "onServicesDiscovered received: " + status);
			broadcastError(GattError.DISCOVER_SERVICES_FAILED);
		}
	}

	@Override
	public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
		if (status == BluetoothGatt.GATT_SUCCESS){
            AppLog.w(TAG, "MTU changed from " + mMTU + " to " + mtu);
			mMTU = mtu;
			broadcastAction(ACTION_MTU_CHANGED, mtu);
		}
		else{
            AppLog.w(TAG, "onMtuChanged received: " + status);
			broadcastError(GattError.MTU_CHANGE_FAILED);
		}
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt,
	                                 BluetoothGattCharacteristic characteristic,
	                                 int status) {
		Log.d(TAG, "In onCharacteristicRead");
		if (status == BluetoothGatt.GATT_SUCCESS) {
			bleOperationDone(OP_READ_CHARA);
			Bundle bundle = new Bundle();
			bundle.putString(EXTRA_GATT_CHARACTERISTIC, characteristic.getUuid().toString());
			bundle.putByteArray(EXTRA_BYTE_VALUE, characteristic.getValue());
			broadcastAction(ACTION_DATA_AVAILABLE, bundle);
		}
		else if(status == BluetoothGatt.GATT_READ_NOT_PERMITTED){
			broadcastError(GattError.GATT_READ_NOT_PERMITTED);
		}
		else{
			broadcastError(GattError.GATT_READ_FAILED);
		}
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		Log.d(TAG, "In onCharacteristicChanged");
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_GATT_CHARACTERISTIC, characteristic.getUuid().toString());
		bundle.putByteArray(EXTRA_BYTE_VALUE, characteristic.getValue());
		broadcastAction(ACTION_DATA_AVAILABLE, bundle);
	}

	@Override
	public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		Log.d(TAG, "In onCharacteristicWrite");
		if(status == BluetoothGatt.GATT_SUCCESS) {
			Bundle bundle = new Bundle();
			bundle.putString(EXTRA_GATT_CHARACTERISTIC, characteristic.getUuid().toString());
			broadcastAction(ACTION_DATA_WRITTEN, bundle);
			bleOperationDone(OP_WRITE_CHARA);
		}
		else if(status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED){
			broadcastError(GattError.GATT_WRITE_NOT_PERMITTED);
		}
		else{
			broadcastError(GattError.GATT_WRITE_FAILED);
		}
	}

	@Override
	public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
		Log.d(TAG, "In onDescriptorWrite");
		if(status == BluetoothGatt.GATT_SUCCESS) {
			// Enable or disable the characteristic's notifications if the descriptor was CCC
			if(descriptor.getUuid().equals(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESC)){
				byte[] data = descriptor.getValue();
				if(data != null && data.length >= 1){
					gatt.setCharacteristicNotification(descriptor.getCharacteristic(), (data[0] > 0));
				}
			}
			Bundle bundle = new Bundle();
			bundle.putString(EXTRA_GATT_DESCRIPTOR, descriptor.getUuid().toString());
			bundle.putString(EXTRA_GATT_CHARACTERISTIC, descriptor.getCharacteristic().getUuid().toString());
			bundle.putByteArray(EXTRA_BYTE_VALUE, descriptor.getValue());
			broadcastAction(ACTION_DESCRIPTOR_WRITTEN, bundle);
			bleOperationDone(OP_WRITE_DESCRIP);
		}
		else if(status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED){
			broadcastError(GattError.GATT_WRITE_NOT_PERMITTED);
		}
		else{
			broadcastError(GattError.GATT_WRITE_FAILED);
		}
	}

	@Override
	public void onDescriptorRead (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
		Log.d(TAG, "In onDescriptorRead");
		if(status == BluetoothGatt.GATT_SUCCESS) {
			// Enable or disable the characteristic's notifications if the descriptor was CCC
			if(descriptor.getUuid().equals(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESC)){
				byte[] data = descriptor.getValue();
				if(data != null && data.length >= 1){
					gatt.setCharacteristicNotification(descriptor.getCharacteristic(), (data[0] > 0));
				}
			}
			Bundle bundle = new Bundle();
			bundle.putString(EXTRA_GATT_DESCRIPTOR, descriptor.getUuid().toString());
			bundle.putString(EXTRA_GATT_CHARACTERISTIC, descriptor.getCharacteristic().getUuid().toString());
			bundle.putByteArray(EXTRA_BYTE_VALUE, descriptor.getValue());
			broadcastAction(ACTION_DESCRIPTOR_READ, bundle);
			bleOperationDone(OP_READ_DESCRIP);
		}
		else if(status == BluetoothGatt.GATT_READ_NOT_PERMITTED){
			broadcastError(GattError.GATT_READ_NOT_PERMITTED);
		}
		else{
			broadcastError(GattError.GATT_READ_FAILED);
		}
	}

	@Override
	public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status){
		if(status == BluetoothGatt.GATT_SUCCESS) {
			broadcastAction(ACTION_READ_RSSI, rssi);
			bleOperationDone(OP_READ_RSSI);
		}
		else{
			broadcastError(GattError.READ_RSSI_FAILED);
		}
	}

	protected void broadcastAction(String action) {
		Log.d(TAG, "Sending broadcast: "+action+mBluetoothDevAddr);
		mBroadcastManager.sendBroadcast(new Intent(action + mBluetoothDevAddr));
	}

	protected void broadcastAction(String action, Bundle bundle) {
		Intent intent = new Intent(action+mBluetoothDevAddr);
		intent.putExtras(bundle);
		mBroadcastManager.sendBroadcast(intent);
	}

	protected void broadcastAction(String action, int data) {
		Intent intent = new Intent(action + mBluetoothDevAddr);
		intent.putExtra(EXTRA_DATA, data);
		mBroadcastManager.sendBroadcast(intent);
	}

	protected void broadcastError(int error) {
		Intent intent = new Intent(ACTION_GATT_ERROR + mBluetoothDevAddr);
		intent.putExtra(EXTRA_ERROR, error);
		mBroadcastManager.sendBroadcast(intent);
	}

	private Runnable mLeDiscoveryDelay = new Runnable() {
		@Override
		public void run() {
			discoverServices();
			mConnectHandler.postDelayed(mLeConnectStopper, DISCOVER_TO_MS);
		}
	};

	/**
	 * Discover the services of the connected BLE device.
	 * @return indicator if this failed or not
	 */
	public int discoverServices(){
		if (!isOkToAddBLEOp()) {
			return -1;
		}

		BleOperation bleop = new BleOperation(OP_DISCOVER_SERVICES, 5000);
		addBleOp(bleop);

		return 0;
	}

	// Runnable to disconnect from the device because it took too long to properly connect.
	private Runnable mLeConnectStopper = new Runnable() {
		@Override
		public void run() {
            AppLog.d(TAG, "Connect timeout!");
			if(mConnectionState == STATE_DISCOVERING){
				broadcastError(GattError.DISCOVER_SERVICES_TIMEOUT);
			}
			else{
				broadcastError(GattError.CONN_TIMEOUT);
			}
			disconnect();
            disconnected();
		}
	};

	public void close(){
		closeGatt();
		if(mOnConnectionClosedListener != null) {
			mOnConnectionClosedListener.onConnectionClosed(this);
		}
	}

	private void closeGatt(){
		if(mBluetoothGatt != null){
			mConnectionState = STATE_DISCONNECTED;
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
	}

	public int getConnectionState(){
		return mConnectionState;
	}

	public BluetoothDevice getDevice(){
		return mBluetoothDev;
	}

	public String getDeviceName(){
		return mDeviceName;
	}

	public String getDeviceAddress(){
		return mBluetoothDevAddr;
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This should be
	 * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null){
			return null;
		}

		return mBluetoothGatt.getServices();
	}

	public BluetoothGattService getGattService(UUID uuid) {
		if (mBluetoothGatt == null){
			return null;
		}

		return mBluetoothGatt.getService(uuid);
	}

    public void connect(boolean autoconnect){
//        if(mConnectionState == STATE_DISCONNECTED){
//            closeGatt();
//        }
        mConnectHandler.removeCallbacksAndMessages(null);
        closeGatt();

        AppLog.d(TAG, "Connecting to "+mBluetoothDevAddr+"...");
        mConnectionState = STATE_CONNECTING;
        Handler handler = new Handler(mContext.getMainLooper());
        if(autoconnect) {
            if (Build.VERSION.SDK_INT >= 23) {
                mBluetoothGatt = mBluetoothDev.connectGatt(mContext, true, BLEConnection.this, BluetoothDevice.TRANSPORT_LE);
            } else {
                mBluetoothGatt = mBluetoothDev.connectGatt(mContext, true, BLEConnection.this);
            }
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (Build.VERSION.SDK_INT >= 23) {
//                        mBluetoothGatt = mBluetoothDev.connectGatt(mContext, true, BLEConnection.this, BluetoothDevice.TRANSPORT_LE);
//                    } else {
//                        mBluetoothGatt = mBluetoothDev.connectGatt(mContext, true, BLEConnection.this);
//                    }
//                }
//            });
        }
        else{
            if(Build.VERSION.SDK_INT >= 23) {
                mBluetoothGatt = mBluetoothDev.connectGatt(mContext, false, BLEConnection.this, BluetoothDevice.TRANSPORT_LE);
            }
            else{
                mBluetoothGatt = mBluetoothDev.connectGatt(mContext, false, BLEConnection.this);
            }
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if(Build.VERSION.SDK_INT >= 23) {
//                        mBluetoothGatt = mBluetoothDev.connectGatt(mContext, false, BLEConnection.this, BluetoothDevice.TRANSPORT_LE);
//                    }
//                    else{
//                        mBluetoothGatt = mBluetoothDev.connectGatt(mContext, false, BLEConnection.this);
//                    }
//                }
//            });
        }
		if(mBluetoothGatt==null){
			AppLog.e(TAG, "mBluetoothGatt is null!");
		}
        mConnectHandler.postDelayed(mLeConnectStopper, CONNECT_TO_MS);
        broadcastAction(ACTION_GATT_CONNECTING);
    }

	public void connect(){
        connect(false);
//		if(mConnectionState == STATE_DISCONNECTED){
//			closeGatt();
//		}
//
//		Log.d(TAG, "Connecting to "+mBluetoothDevAddr+"...");
//		mConnectionState = STATE_CONNECTING;
//		Handler handler = new Handler(Looper.getMainLooper());
//		handler.post(new Runnable() {
//			@Override
//			public void run() {
//				if(Build.VERSION.SDK_INT >= 23) {
//					mBluetoothGatt = mBluetoothDev.connectGatt(mContext, false, BLEConnection.this, BluetoothDevice.TRANSPORT_LE);
//				}
//				else{
//					mBluetoothGatt = mBluetoothDev.connectGatt(mContext, false, BLEConnection.this);
//				}
//			}
//		});
//
//		mConnectHandler.postDelayed(mLeConnectStopper, CONNECT_TO_MS);
//		broadcastAction(ACTION_GATT_CONNECTING);
	}

	public void disconnect(){
		AppLog.d(TAG, "Told to disconnect!");
		if(mBluetoothGatt != null){
			if(mConnectionState >= STATE_CONNECTING){
				clearBLEOperationsQueue();
				mConnectionState = STATE_DISCONNECTING;
				mBluetoothGatt.disconnect();
				broadcastAction(ACTION_GATT_DISCONNECTING);
				mMTU = MTU_DEFAULT;
			}
		}
	}

    private void disconnected(){
        mConnectHandler.removeCallbacksAndMessages(null);
        clearBLEOperationsQueue();
        refreshDeviceCache();
        closeGatt();
        mConnectionState = STATE_DISCONNECTED;
        mMTU = MTU_DEFAULT;
    }

	public void clearBLEOperationsQueue(){
		synchronized (mBleOps) {
			mBleOpRetryHandler.removeCallbacksAndMessages(null);
			mBleOps.clear();
			mBleOpTries = 0;
		}
	}

	/**
	 * Refresh the cached information on the device
	 * @return true if this suceeded
	 */
	public boolean refreshDeviceCache(){
		if(mBluetoothGatt != null) {
			try {
				Method localMethod = mBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
				if (localMethod != null) {
					boolean bool = ((Boolean) localMethod.invoke(mBluetoothGatt, new Object[0])).booleanValue();
					return bool;
				}
			} catch (Exception localException) {
                AppLog.e(TAG, "An exception occurred while refreshing device");
			}
		}
		return false;
	}

	/**
	 * Queue up a command to read the device's RSSI value.
	 * @return indicator if this failed or not
	 */
	public int readRemoteRSSI()
	{
		// Check if there is already 2 or more RSSI ops in the queue so we don't flood the queue
		Iterator<BleOperation> it = mBleOps.iterator();
		int numRssiOps = 0;
		while(it.hasNext()){
			BleOperation bleop = it.next();
			if(bleop.opType == OP_READ_RSSI){
				numRssiOps++;
				if(numRssiOps >= 2){
					return 0;
				}
			}
		}

		// Check if we can add a BLE operation to the queue
		if (!isOkToAddBLEOp()) {
			return -1;
		}

		// Create the BLE operation and add it to the queue
		BleOperation bleop = new BleOperation(OP_READ_RSSI);
		addBleOp(bleop);
		return 0;
	}

	/**
	 * Queue up a command to write to BLE characteristic.
	 * @param gattCharacteristic the characteristic to write to
	 * @param bytes the bytes to set the characteristic to
	 * @return indicator if this failed or not
	 */
	public int writeChara(BluetoothGattCharacteristic gattCharacteristic, byte[] bytes)
	{
		if (!isOkToAddBLEOp()) {
			return -1;
		}

		BleOperation bleop = new BleOperation(OP_WRITE_CHARA, gattCharacteristic, bytes);
		addBleOp(bleop);

		return 0;
	}

	/**
	 * Queue up a command to write to BLE characteristic, but don't flood the queue with similar
	 * write commands. This modifies the next write to this characteristic to use the new bytes
	 * array and removes other write commands to this characteristic from the queue.
	 * @param gattCharacteristic the characteristic to write to
	 * @param bytes the bytes to set the characteristic to
	 * @return indicator if this failed or not
	 */
	public int writeCharaNF(BluetoothGattCharacteristic gattCharacteristic, byte[] bytes)
	{
		int pos = 0;
		boolean opModified = false;

		synchronized (mBleOps) {
			Iterator<BleOperation> it = mBleOps.iterator();
			while(it.hasNext()){
				BleOperation bleop = it.next();
				if(pos != 0 && bleop.opType == OP_WRITE_CHARA && bleop.characteristic == gattCharacteristic){
					if(!opModified){
						bleop.writeBytes = bytes;
						opModified = true;
					}
					else {
						it.remove();
					}
				}
				pos++;
			}
		}

		if(!opModified) {
			if (!isOkToAddBLEOp()) {
				return -1;
			}

			BleOperation bleop = new BleOperation(OP_WRITE_CHARA, gattCharacteristic, bytes);
			addBleOp(bleop);
		}

		return 0;
	}

	/**
	 * Read characteristic data.
	 * @param gattCharacteristic    the characteristic to read
	 */
	public int readChara(BluetoothGattCharacteristic gattCharacteristic){
		if (!isOkToAddBLEOp()) {
			return -1;
		}

		BleOperation bleop = new BleOperation(OP_READ_CHARA, gattCharacteristic);
		addBleOp(bleop);

		return 0;
	}

	/**
	 * Read characteristic data, no flood queue version.
	 * @param gattCharacteristic    the characteristic to read
	 */
	public int readCharaNF(BluetoothGattCharacteristic gattCharacteristic){
		synchronized (mBleOps) {
			// Check if there is already 2 or more read ops in the queue so we don't flood the queue
			Iterator<BleOperation> it = mBleOps.iterator();
			int numReadOps = 0;
			while (it.hasNext()) {
				BleOperation bleop = it.next();
				if (bleop.opType == OP_READ_CHARA && bleop.characteristic == gattCharacteristic) {
					numReadOps++;
					if (numReadOps >= 2) {
						return 0;
					}
				}
			}
		}

		if (!isOkToAddBLEOp()) {
			return -1;
		}

		BleOperation bleop = new BleOperation(OP_READ_CHARA, gattCharacteristic);
		addBleOp(bleop);

		return 0;
	}

	/**
	 * Enable/disable the characteristic giving notifications
	 * @param gattCharacteristic the characteristic to set
	 * @param notifData byte array to set the notification descriptor to
	 * @return indicator if this failed or not
	 */
//	public int setCharaNotif(BluetoothGattCharacteristic gattCharacteristic, byte[] notifData)
//	{
//		if (!isOkToAddBLEOp()) {
//			return -1;
//		}
//
//		BleOperation bleop = new BleOperation(OP_SET_NOTIF, gattCharacteristic, notifData);
//		addBleOp(bleop);
//
//		return 0;
//	}

	/**
	 * Enable/disable the characteristic giving notifications
	 * @param characteristic the characteristic to set
	 * @param enable enable or disable notifications
	 * @return indicator if this failed or not
	 */
	public int setCharaNotification(BluetoothGattCharacteristic characteristic, boolean enable)
	{
		BluetoothGattDescriptor cccd = characteristic.getDescriptor(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESC);
		if (cccd != null) {
			byte[] cccdData = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
			if (cccd.getValue() != null && cccd.getValue().length >= 1) {
				if (enable) {
					cccdData[0] = (byte) (cccd.getValue()[0] | BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0]);
				}
				else{
					cccdData[0] = (byte) (cccd.getValue()[0] & ~(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0]));
				}
			}
			else if(enable){
				cccdData = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
			}

			return writeDescrip(cccd, cccdData);
		}

		return -1;
	}

	/**
	 * Enable/disable the characteristic giving indications
	 * @param characteristic the characteristic to set
	 * @param enable enable or disable indications
	 * @return indicator if this failed or not
	 */
	public int setCharaIndication(BluetoothGattCharacteristic characteristic, boolean enable)
	{
		BluetoothGattDescriptor cccd = characteristic.getDescriptor(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESC);
		if (cccd != null) {
			byte[] cccdData = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
			if (cccd.getValue() != null && cccd.getValue().length >= 1) {
				if (enable) {
					cccdData[0] = (byte) (cccd.getValue()[0] | BluetoothGattDescriptor.ENABLE_INDICATION_VALUE[0]);
				}
				else{
					cccdData[0] = (byte) (cccd.getValue()[0] & ~(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE[0]));
				}
			}
			else if(enable){
				cccdData = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
			}

			return writeDescrip(cccd, cccdData);
		}

		return -1;
	}

	/**
	 * Enable/disable the characteristic giving notifications, no flood queue version
	 * @param gattCharacteristic the characteristic to set
	 * @param notifData byte array to set the notification descriptor to
	 * @return indicator if this failed or not
	 */
//	public int setCharaNotifNF(BluetoothGattCharacteristic gattCharacteristic, byte[] notifData)
//	{
//		int pos = 0;
//		boolean opModified = false;
//
//		synchronized (mBleOps) {
//			Iterator<BleOperation> it = mBleOps.iterator();
//			while(it.hasNext()){
//				BleOperation bleop = it.next();
//				if(pos != 0 && bleop.opType == OP_SET_NOTIF && bleop.characteristic == gattCharacteristic){
//					if(!opModified){
//						bleop.writeBytes = notifData;
//						opModified = true;
//					}
//					else {
//						it.remove();
//					}
//				}
//				pos++;
//			}
//		}
//
//		if(!opModified) {
//			if (!isOkToAddBLEOp()) {
//				return -1;
//			}
//
//			BleOperation bleop = new BleOperation(OP_SET_NOTIF, gattCharacteristic, notifData);
//			addBleOp(bleop);
//		}
//
//		return 0;
//	}

	/**
	 * Enable/disable the characteristic giving notifications, no flood queue version
	 * @param gattCharacteristic the characteristic to set
	 * @param enable enable or disable notifications
	 * @return indicator if this failed or not
	 */
//	public int setCharaNotifNF(BluetoothGattCharacteristic gattCharacteristic, boolean enable)
//	{
//		byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
//		if(enable){
//			data[0] = (byte)(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0] | BluetoothGattDescriptor.ENABLE_INDICATION_VALUE[0]);
//		}
//
//		return setCharaNotifNF(gattCharacteristic, data);
//	}

	/**
	 * Queue up a command to write to BLE descriptor.
	 * @param descriptor the descriptor to write to
	 * @param bytes the bytes to set the descriptor to
	 * @return indicator if this failed or not
	 */
	public int writeDescrip(BluetoothGattDescriptor descriptor, byte[] bytes)
	{
		if (!isOkToAddBLEOp()) {
			return -1;
		}

		BleOperation bleop = new BleOperation(OP_WRITE_DESCRIP, descriptor, bytes);
		addBleOp(bleop);

		return 0;
	}

	/**
	 * Read descriptor data.
	 * @param descriptor    the descriptor to read
	 */
	public int readDescrip(BluetoothGattDescriptor descriptor){
		if (!isOkToAddBLEOp()) {
			return -1;
		}

		BleOperation bleop = new BleOperation(OP_READ_DESCRIP, descriptor);
		addBleOp(bleop);

		return 0;
	}

	/**
	 * Get the maximum number of bytes that can be transferred to the device.
	 * @return the number of bytes that can be transferred in a single packet
	 */
	public int getAvailableMTU(){
		return mMTU - MTU_OVERHEAD_BYTES;
	}

	public int getMTU(){
		return mMTU;
	}

	public int  requestMTUSize(int mtu){
		if (!isOkToAddBLEOp()) {
			return -1;
		}

		// Create the BLE operation and add it to the queue
		BleOperation bleop = new BleOperation(OP_SET_MTU);
		bleop.writeBytes = ByteBuffer.allocate(4).putInt(mtu).array();
		addBleOp(bleop);

		return 0;
	}

	/**
	 * Request a write with no response on a given
	 * {@code BluetoothGattCharacteristic}.
	 *
	 * @param characteristic
	 */
	private int _writeChara(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothGatt == null || mConnectionState != STATE_READY) {
			return -1;
		} else {
			mBluetoothGatt.writeCharacteristic(characteristic);
		}
		return 0;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
	 * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic The characteristic to read from.
	 */
	private int _readChara(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothGatt == null || mConnectionState != STATE_READY || (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0) {
			return -1;
		}

		mBluetoothGatt.readCharacteristic(characteristic);

		return 0;
	}

	/**
	 * Request a write with no response on a given
	 * {@code BluetoothGattDescriptor}.
	 *
	 * @param descriptor
	 */
	private int _writeDescrip(BluetoothGattDescriptor descriptor) {
		if (mBluetoothGatt == null || mConnectionState != STATE_READY) {
			return -1;
		} else {
			// Enable or disable the characteristic's notifications if the descriptor was CCC
			if(descriptor.getUuid().equals(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESC)){
				byte[] data = descriptor.getValue();
				if(data != null && data.length >= 1){
					mBluetoothGatt.setCharacteristicNotification(descriptor.getCharacteristic(), (data[0] > 0));
				}
			}
			mBluetoothGatt.writeDescriptor(descriptor);
		}
		return 0;
	}

	/**
	 * Request a read on a given {@code BluetoothGattDescriptor}. The read result is reported
	 * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param descriptor The descriptor to read from.
	 */
	private int _readDescrip(BluetoothGattDescriptor descriptor) {
		if (mBluetoothGatt == null || mConnectionState != STATE_READY) {
			return -1;
		}

		mBluetoothGatt.readDescriptor(descriptor);

		return 0;
	}

	/**
	 * Internal function to read the device's RSSI value.
	 * @return indicator if this failed or not
	 */
	private int _readRemoteRssi(){
		if (mBluetoothGatt == null || mConnectionState < STATE_CONNECTED) {
			return -1;
		}

		mBluetoothGatt.readRemoteRssi();

		return 0;
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic Characteristic to act on.
	 * @param value The value to write to the CCCD
	 */
	private int _setCharaNotif(BluetoothGattCharacteristic characteristic, byte[] value) {
		if (mBluetoothGatt == null || mConnectionState != STATE_READY) {
			return -1;
		}

		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESC);

		if (descriptor != null) {
			Log.d(TAG, "Writing to descriptor");
			descriptor.setValue(value);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
		else{
			Log.w(TAG, "Descriptor is null!");
		}

		return 0;
	}

	/**
	 * Discover the device's services. This must be performed before we can use any of the services
	 * or characteristics.
	 * @return indicator if this failed or not
	 */
	private int _discoverServices() {
		if (mBluetoothGatt == null || mConnectionState < STATE_CONNECTED) {
			return -1;
		}
        AppLog.d(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
		mConnectionState = STATE_DISCOVERING;
		broadcastAction(ACTION_GATT_DISCOVERING_SERVICES);

		return 0;
	}

	private int _requestMTUSize(int mtu){
		if (mBluetoothGatt == null || mConnectionState < STATE_CONNECTED) {
			return -1;
		}

		if(mtu > MTU_MAX) {
			mtu = MTU_MAX;
		}

		if(Build.VERSION.SDK_INT >= 21) {
			mBluetoothGatt.requestMtu(mtu);
		}
		else {
			return -1;
		}
		return 0;
	}

	/**
	 * Adding the necessary Intent filters for Broadcast receivers
	 *
	 * @return {@link IntentFilter}
	 */
	public IntentFilter makeGattUpdateIntentFilter() {
		return makeGattUpdateIntentFilter(mBluetoothDevAddr);
	}

	/**
	 * Adding the necessary Intent filters for Broadcast receivers
	 *
	 * @return {@link IntentFilter}
	 */
	public static IntentFilter makeGattUpdateIntentFilter(String devAddress) {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_GATT_CONNECTED+devAddress);
		intentFilter.addAction(ACTION_GATT_CONNECTING+devAddress);
		intentFilter.addAction(ACTION_GATT_DISCONNECTING+devAddress);
		intentFilter.addAction(ACTION_GATT_DISCONNECTED+devAddress);
		intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED+devAddress);
		intentFilter.addAction(ACTION_GATT_DISCOVERING_SERVICES+devAddress);
		intentFilter.addAction(ACTION_DATA_WRITTEN+devAddress);
		intentFilter.addAction(ACTION_DATA_AVAILABLE+devAddress);
		intentFilter.addAction(ACTION_READ_RSSI+devAddress);
		intentFilter.addAction(ACTION_DESCRIPTOR_WRITTEN+devAddress);
		intentFilter.addAction(ACTION_DESCRIPTOR_READ+devAddress);
		intentFilter.addAction(ACTION_MTU_CHANGED+devAddress);
		intentFilter.addAction(ACTION_GATT_ERROR+devAddress);
		return intentFilter;
	}

	/**
	 * Check if it is ok to add a BLE operation to the queue.
	 * @return true if it's ok to add a BLE operation, false otherwise
	 */
	private boolean isOkToAddBLEOp(){
		return (mBluetoothGatt != null && mConnectionState >= STATE_CONNECTED && mBleOps.size() < MAX_BLEOP_QUEUE_SIZE);
	}

	/**
	 * Perform the next BLE operation.
	 */
	private void doNextBleOp(){
		int bleRet = -1;
		mBleOpRetryHandler.removeCallbacksAndMessages(null);
		synchronized (mBleOps) {
			// Check if we have tried this operation too many times
			if (!mBleOps.isEmpty() && ++mBleOpTries > MAX_BLEOP_TRIES) {
				mBleOps.remove();
				mBleOpTries = 1;
				mBleOpFailures++;
			}
			while (!mBleOps.isEmpty() && bleRet < 0) {
				// Check if there was an error performing the operation
				if ((bleRet = mBleOps.peek().execute()) < 0) {
					mBleOps.remove();
				} else {
					// Set the handler for a timeout performing this operation
					mBleOpRetryHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							doNextBleOp();
						}
					}, mBleOps.peek().timeout_ms);
				}
			}

			if (mBleOps.isEmpty()) {

			}
		}
	}

	/**
	 * Add the BLE operation to the queue. Run it if it is the only one in the queue.
	 * @param bleop the BLE operation to add to the queue
	 * @return
	 */
	private void addBleOp(BleOperation bleop){
		synchronized (mBleOps) {
			mBleOps.add(bleop);
			if (mBleOps.size() == 1) {
				doNextBleOp();
			}
		}
	}

	/**
	 * Finished a BLE operation so remove it from the queue and execute the next one.
	 * @param bleOp the BLE operation type that just finished
	 */
	public void bleOperationDone(int bleOp){
		synchronized (mBleOps) {
			if (!mBleOps.isEmpty()) {
				int opType = mBleOps.peek().opType;
				if (opType == bleOp) {
					mBleOps.remove();
				}
			}
			mBleOpTries = 0;
		}
		doNextBleOp();
	}

	/**
	 * BLE operation class
	 */
	private class BleOperation {
		// operation type
		public int opType;
		// the BLE characteristic to operate on
		public BluetoothGattCharacteristic characteristic;
		// the BLE descriptor to operate on
		public BluetoothGattDescriptor descriptor;
		// the bytes to write to the characteristic (if necessary)
		public byte[] writeBytes = null;
		// timeout for this operation
		public int timeout_ms = 1500;

		public BleOperation(){
		}

		public BleOperation(int opType){
			this.opType = opType;
		}

		public BleOperation(int opType, int timeout){
			this.opType = opType;
			this.timeout_ms = timeout;
		}

		public BleOperation(int opType, BluetoothGattCharacteristic characteristic){
			this.opType = opType;
			this.characteristic = characteristic;
		}

		public BleOperation(int opType, BluetoothGattCharacteristic characteristic, int timeout){
			this.opType = opType;
			this.characteristic = characteristic;
			this.timeout_ms = timeout;
		}

		public BleOperation(int opType, BluetoothGattCharacteristic characteristic, byte[] writeData){
			this.opType = opType;
			this.characteristic = characteristic;
			this.writeBytes = writeData;
		}

		public BleOperation(int opType, BluetoothGattCharacteristic characteristic, byte[] writeData, int timeout){
			this.opType = opType;
			this.characteristic = characteristic;
			this.writeBytes = writeData;
			this.timeout_ms = timeout;
		}

		public BleOperation(int opType, BluetoothGattDescriptor descriptor){
			this.opType = opType;
			this.descriptor = descriptor;
		}

		public BleOperation(int opType, BluetoothGattDescriptor descriptor, int timeout){
			this.opType = opType;
			this.descriptor = descriptor;
			this.timeout_ms = timeout;
		}

		public BleOperation(int opType, BluetoothGattDescriptor descriptor, byte[] writeData){
			this.opType = opType;
			this.descriptor = descriptor;
			this.writeBytes = writeData;
		}

		public BleOperation(int opType, BluetoothGattDescriptor descriptor, byte[] writeData, int timeout){
			this.opType = opType;
			this.descriptor = descriptor;
			this.writeBytes = writeData;
			this.timeout_ms = timeout;
		}

		// Execute this BLE operation
		public int execute() {
			int retval = -1;
			// Return if not connected to a BLE device
			if (mBluetoothGatt == null) {
				return retval;
			}
			switch (opType) {
				case OP_WRITE_CHARA:
					if (characteristic == null) {
						return -1;
					}
					Log.d(TAG, "Writing to " + characteristic.getUuid().toString());
					if (writeBytes == null) {
						retval = _writeChara(characteristic);
					} else {
						characteristic.setValue(writeBytes);
						retval = _writeChara(characteristic);
					}
					break;
				case OP_READ_CHARA:
					if (characteristic == null) {
						return -1;
					}
					Log.d(TAG, "Reading characteristic " + characteristic.getUuid().toString());
					retval = _readChara(characteristic);
					break;
//				case OP_SET_NOTIF:
//					if (characteristic == null) {
//						return -1;
//					}
//					Log.d(TAG, "Setting notifications for characteristic " + characteristic.getUuid().toString());
//					retval = _setCharaNotif(characteristic, writeBytes);
//					break;
				case OP_READ_RSSI:
					Log.d(TAG, "Reading RSSI value");
					retval = _readRemoteRssi();
					break;
				case OP_DISCOVER_SERVICES:
					Log.d(TAG, "Discovering services");
					retval = _discoverServices();
					break;
				case OP_SET_MTU:
					if (writeBytes == null) {
						return -1;
					}
					Log.d(TAG, "Requesting MTU size");
					retval = _requestMTUSize(ByteBuffer.wrap(writeBytes).getInt());
					break;
				case OP_WRITE_DESCRIP:
					if (descriptor == null) {
						return -1;
					}
					Log.d(TAG, "Writing to " + descriptor.getUuid().toString());
					if (writeBytes == null) {
						retval = _writeDescrip(descriptor);
					} else {
						descriptor.setValue(writeBytes);
						retval = _writeDescrip(descriptor);
					}
					break;
				case OP_READ_DESCRIP:
					if (descriptor == null) {
						return -1;
					}
					Log.d(TAG, "Reading descriptor " + descriptor.getUuid().toString());
					retval = _readDescrip(descriptor);
					break;
				default:
					break;
			}

			return retval;
		}
	}
}
