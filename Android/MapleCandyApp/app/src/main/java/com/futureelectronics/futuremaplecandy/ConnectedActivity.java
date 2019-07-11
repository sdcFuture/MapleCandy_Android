package com.futureelectronics.futuremaplecandy;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class ConnectedActivity extends AppCompatActivity implements DashboardFragment.MapleCandyWriteChar {

    private final static String TAG = ConnectedActivity.class.getSimpleName();
    private BLEConnection mConnection;
    private BluetoothLeService mBLEService;

    private Fragment[] mFragments = new Fragment[2];

    /**
     * Bluetooth characteristics needed to interact with Maple Candy
     */
    private BluetoothGattCharacteristic mMapleCandyWriteChar;
    public static BluetoothGattCharacteristic mVUART_Chara;

    private TextView mTitle;

    /**
     * Code to manage Service life cycle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEService = ((BluetoothLeService.LocalBinder) service).getService();
            boolean connValid = false;

            Bundle args = getIntent().getExtras();
            if(args != null){
                if(args.containsKey(Constants.EXTRA_DEV_ADDRESS)){
                    mConnection = mBLEService.getDeviceConnection(args.getString(Constants.EXTRA_DEV_ADDRESS));
                    if(mConnection != null){
                        LocalBroadcastManager.getInstance(ConnectedActivity.this).registerReceiver(mGattUpdateReceiver, mConnection.makeGattUpdateIntentFilter());
                        connValid = getGattCharacteristics();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                String title = mConnection.getDeviceName();
//                                if(title == null || title.isEmpty()){
//                                    title = mConnection.getDeviceAddress();
//                                }
//                                setTitle(title);
//                            }
//                        });
                    }
                }
            }

            if(!connValid){
                AppLog.i(TAG, "BLE is not connected to a valid device!");
                Toast.makeText(ConnectedActivity.this, getString(R.string.msg_not_valid_device), Toast.LENGTH_LONG).show();
                if(mConnection != null){
                    mConnection.disconnect();
                }
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();

            // Connected to GATT Server
            if (action.startsWith(BLEConnection.ACTION_GATT_CONNECTED)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_CONNECTING)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_DISCONNECTING)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_DISCONNECTED)) {
                AppLog.i(TAG, "GATT disconnected.");
                LocalBroadcastManager.getInstance(ConnectedActivity.this).unregisterReceiver(mGattUpdateReceiver);
                Toast.makeText(ConnectedActivity.this, getString(R.string.msg_err_device_disconnected), Toast.LENGTH_LONG).show();
                finish();
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_SERVICES_DISCOVERED)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_DISCOVERING_SERVICES)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_DATA_AVAILABLE)) {
                UUID charaUuid = UUID.fromString(extras.getString(BLEConnection.EXTRA_GATT_CHARACTERISTIC));
                AppLog.e(TAG, "DATA Available!");
                if (charaUuid.equals(mVUART_Chara.getUuid())) {
                    byte[] data = extras.getByteArray(BLEConnection.EXTRA_BYTE_VALUE);

                    AppLog.i(TAG, "Received Accel Gyro data: " + Utils.bytesToString(data));
                    AppLog.i(TAG, "Received Accel Gyro data: " + data);
                    if (data != null && data.length >= 2) {
                        //((DashboardFragment) mFragments[0]).onRPMUpdate(Utils.bytesTo16bitIntLE(data, 0));
                        //((DashboardFragment) mFragments[0]).onRPMUpdate(data);
                        float[] adcVals = new float[3];
                        float[] gyroVals = new float[3];

                        adcVals[0] = (float) Utils.bytesTo16bitIntLE(data, 0) * (3.3f / 4095);
                        adcVals[1] = (float) Utils.bytesTo16bitIntLE(data, 2) * (10.0f / 4095);
                        adcVals[2] = (float) Utils.bytesTo16bitIntLE(data, 4) * (20.0f / 4095);

                        ((DashboardFragment) mFragments[0]).onUpdateAccelGyro(adcVals, data);
                    }
                }
            }
            else if (action.startsWith(BLEConnection.ACTION_DATA_WRITTEN)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_DESCRIPTOR_WRITTEN)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_DESCRIPTOR_READ)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_READ_RSSI)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_MTU_CHANGED)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_ERROR)) {
                int err = extras.getInt(BLEConnection.EXTRA_ERROR);

                switch (err) {
                    case GattError.CONN_TIMEOUT:
                    case GattError.DISCOVER_SERVICES_TIMEOUT:
                    case GattError.DISCOVER_SERVICES_FAILED:

                        break;
                }
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    if(mFragments[0] == null){
                        mFragments[0] = DashboardFragment.newInstance();
                    }
                    displayView(mFragments[0], Constants.FRAGTAG_DASHBOARD, false);
                    return true;
//                case R.id.navigation_xxxx: //Other menu options
//
//                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
//        mTitle = getSupportActionBar().getCustomView().findViewById(R.id.custom_actionbar_title);

//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Intent service = new Intent(this, BluetoothLeService.class);
        startService(service);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);

        if(savedInstanceState == null){
            if(mFragments[0] == null){
                mFragments[0] = DashboardFragment.newInstance();
            }
            displayView(mFragments[0], Constants.FRAGTAG_DASHBOARD, false);
        }
        else{
            FragmentManager fm = getSupportFragmentManager();
            mFragments[0] = fm.findFragmentByTag(Constants.FRAGTAG_DASHBOARD);
//            mFragments[1] = fm.findFragmentByTag(Constants.FRAGTAG_LEDCONTROL);
        }

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    @Override
    protected void onResume()
    {
        AppLog.i(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        AppLog.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        AppLog.i(TAG, "onDestroy");
        if(mConnection != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
            mConnection = null;
        }

        unbindService(mServiceConnection);

        super.onDestroy();
    }


    protected void displayView(Fragment fragment, String tag) {
        displayView(fragment, tag, true);
    }

    protected void displayView(Fragment fragment, String tag, boolean saveToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.replace(R.id.content, fragment, tag);

        if(saveToBackStack) {
            // Add this transaction to the back stack, so when the user presses back,
            // it rollbacks.
            ft.addToBackStack(null);
        }
//		mCurrFragTag = tag;
        ft.commit();
    }

    protected boolean getGattCharacteristics()
    {
        if(mConnection == null){
            return false;
        }

        BluetoothGattService maplecandyServ = mConnection.getGattService(UUIDDatabase.UUID_MAPLECANDY_CUSTOM_SERVICE);

        if(maplecandyServ == null){
            AppLog.i(TAG, "Unable to get Maple Candy Custom Service!");
            return false;
        }

        mVUART_Chara = maplecandyServ.getCharacteristic(UUIDDatabase.UUID_MAPLECANDY_VUART_CHAR);
        mMapleCandyWriteChar = maplecandyServ.getCharacteristic(UUIDDatabase.UUID_MAPLECANDY_WRITE_CHAR);

        if(mVUART_Chara == null){
            AppLog.i(TAG, "Unable to get all Maple Candy characteristics!");
            return false;
        }

        mConnection.setCharaIndication(mVUART_Chara, true);

        return true;
    }

        public void onSetMapleCandy_WriteChar(byte[] data){
        if(mConnection != null && mMapleCandyWriteChar != null){

            mConnection.writeChara(mMapleCandyWriteChar, data);

        }
    }

}
