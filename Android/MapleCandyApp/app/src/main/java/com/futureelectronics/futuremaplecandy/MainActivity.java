package com.futureelectronics.futuremaplecandy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.futureelectronics.futuremaplecandy.scan.BleScanner;
import com.futureelectronics.futuremaplecandy.scan.ScannedDevice;
import com.futureelectronics.futuremaplecandy.views.ScanDeviceView;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, BleScanner.BatchDevicesListener,
        ScanDeviceView.ScanDeviceClickListener{
    private final static String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 101;
    private static final int REQUEST_ENABLE_BT = 102;

    private RecyclerView mScanRecyclerView;
    private ScanDeviceAdapter mScanDeviceAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private FloatingActionButton mFab;

    public static BLEConnection mConnection;
    private ProgressBar mProgressBar;


    // Stops scanning after 45 seconds.
    private static final long SCAN_PERIOD = 45000;

    private TextView mTitle;

    /**
     * Code to manage Service life cycle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEService = ((BluetoothLeService.LocalBinder) service).getService();
            AppLog.i(TAG, "Initializing the BLE Service!");
            // Initializing the service
            int btResult = mBLEService.initialize();

            if (btResult != BluetoothLeService.BT_OK) {
                AppLog.e(TAG, "Service not initialized");

                if (btResult == BluetoothLeService.BT_DISABLED) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
            else{
                if(!mBLEService.isScanning()){
                    mSwipeRefresh.setRefreshing(true);
                    onRefresh();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEService = null;
        }
    };

    /**
     * Used to manage connections of the Bluetooth LE Device
     */
    private BluetoothLeService mBLEService;

    /**
     * BroadcastReceiver for receiving the GATT server status
     */
    private final BroadcastReceiver mBTUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            // Get service ready broadcast
            if(BluetoothLeService.ACTION_SERVICE_READY.equals(action)) {
                AppLog.i(TAG, "BLE service ready!");
                if(mBLEService!=null && !mBLEService.isScanning()){
                    mSwipeRefresh.setRefreshing(true);
                    onRefresh();
                }
            }
            else if (BluetoothLeService.ACTION_FINISHED_SCAN.equals(action)) {
                AppLog.i(TAG, "Finished scanning");
                stopScanning();
            }
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();

            // Connected to GATT Server
            if (action.startsWith(BLEConnection.ACTION_GATT_CONNECTED)) {
                AppLog.i(TAG, "Connected, start service discovery");
                discoveringServices();
                stopScanning();
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_CONNECTING)) {
                AppLog.i(TAG, "Connecting...");
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_DISCONNECTING)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_DISCONNECTED)) {
                AppLog.i(TAG, "GATT disconnected.");
                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    mProgressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_err_device_disconnected), BaseTransientBottomBar.LENGTH_LONG).setAction("Action", null).show();
                }
                stopScanning();
                LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mGattUpdateReceiver);
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_SERVICES_DISCOVERED)) {
                AppLog.i(TAG, "Services discovered, now processing");
                stopScanning();
                mProgressBar.setVisibility(View.GONE);

                if(mConnection != null){
                    Intent i = new Intent(MainActivity.this, ConnectedActivity.class);
                    i.putExtra(Constants.EXTRA_DEV_ADDRESS, mConnection.getDeviceAddress());

                    LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mGattUpdateReceiver);
                    mConnection = null;

                    startActivity(i);
                }
            }
            else if (action.startsWith(BLEConnection.ACTION_GATT_DISCOVERING_SERVICES)) {
            }
            else if (action.startsWith(BLEConnection.ACTION_DATA_AVAILABLE)) {
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
                        mProgressBar.setVisibility(View.GONE);
                        try {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.device_cannot_connect), BaseTransientBottomBar.LENGTH_SHORT).setAction("Action", null).show();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
//        mTitle = getSupportActionBar().getCustomView().findViewById(R.id.custom_actionbar_title);

        Intent service = new Intent(this, BluetoothLeService.class);
        startService(service);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBTUpdateReceiver, BluetoothLeService.makeBTUpdateIntentFilter());

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopScanning();
            }
        });
        mFab.setVisibility(View.GONE);

        mScanRecyclerView = (RecyclerView)findViewById(R.id.scanRecyclerView);
        mScanRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mScanDeviceAdapter = new ScanDeviceAdapter(this);
        mScanRecyclerView.setAdapter(mScanDeviceAdapter);

        mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipeContainer);
        mSwipeRefresh.setOnRefreshListener(this);

        mProgressBar = (ProgressBar)findViewById(R.id.connectingProgress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.dia_title_need_location));
                builder.setMessage(getString(R.string.dia_msg_need_location));
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Coarse location permission not granted so quit
                    finish();
                }
                break;

            default:
                break;
        }
    }

    public void onRefresh(){

        mFab.setVisibility(View.VISIBLE);

        mBLEService.disconnectAll();
        mBLEService.startLeScan(this, SCAN_PERIOD);

    }

    public void onDevicesUpdated(List<ScannedDevice> scannedDevices) {
        List<ScannedDevice> filteredDevices = scannedDevices;

        // Remove devices that aren't Maple Candy
        Iterator<ScannedDevice> iterator = filteredDevices.iterator();
        while(iterator.hasNext()){
            String devName = iterator.next().getName();
            if(devName==null || !devName.startsWith("MapleCandy")){
                iterator.remove();
            }
        }

        mScanDeviceAdapter.setScannedDevices(filteredDevices);
    }

    private void stopScanning(){
        mSwipeRefresh.setRefreshing(false);
        mFab.setVisibility(View.GONE);
        if(mBLEService.isScanning()) {
            mBLEService.stopLeScan();
        }
    }

    public void onScanDeviceClicked(ScannedDevice device){
        if(mProgressBar.getVisibility() != View.VISIBLE) {
            AppLog.i(TAG, device.getAddress() + " clicked, trying to connect...");
            connectDevice(device.getBluetoothDevice());
        }

    }

    private void connectDevice(BluetoothDevice device){
        if(device == null){
            return;
        }

        mSwipeRefresh.setRefreshing(false);
        mFab.setVisibility(View.GONE);

        // Connect to the device
        mConnection = mBLEService.createConnection(device);
        // Register the broadcast receiver for connection status
        LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReceiver, mConnection.makeGattUpdateIntentFilter());
        if(!mBLEService.isScanning()) {
            mBLEService.startLeScan(this, 10000);
        }
        mConnection.connect();


        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);

        Snackbar.make(findViewById(android.R.id.content), String.format(getString(R.string.msg_connecting_device), device.getName()), BaseTransientBottomBar.LENGTH_SHORT).setAction("Action", null).show();
    }

    @SuppressLint("WrongConstant")
    private void discoveringServices()
    {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.alert_connected_discovering_msg), BaseTransientBottomBar.LENGTH_SHORT).setAction("Action", null).show();
    }

    @Override
    protected void onDestroy() {
        if (mBLEService != null) {
            if(isFinishing()){
                AppLog.i(TAG, "App is finishing so stopping the BLE service.");
                mBLEService.stopService();
            }
            mBLEService = null;
        }
        unbindService(mServiceConnection);

        if(mConnection != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
            mConnection = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBTUpdateReceiver);

        super.onDestroy();
    }
}
