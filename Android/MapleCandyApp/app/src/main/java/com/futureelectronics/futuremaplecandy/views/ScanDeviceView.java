package com.futureelectronics.futuremaplecandy.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.futureelectronics.futuremaplecandy.AppLog;
import com.futureelectronics.futuremaplecandy.R;
import com.futureelectronics.futuremaplecandy.scan.ScannedDevice;

/**
 * Created by Kyle.Harman on 2/25/2016.
 */
public class ScanDeviceView extends CardView implements View.OnClickListener {
	private final static String TAG = ScanDeviceView.class.getSimpleName();
	private View mMainView;
	private TextView mTxtDevName;
	private TextView mTxtDevAddr;
	private TextView mTxtDevRssi;
	private ScannedDevice mDevice;
    private ScanDeviceClickListener mClickListener = null;

	public ScanDeviceView(Context context) {
		super(context);
	}

	public ScanDeviceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScanDeviceView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public interface ScanDeviceClickListener {
        void onScanDeviceClicked(ScannedDevice device);
    }

	protected void onFinishInflate() {
		super.onFinishInflate();
		if (!isInEditMode()) {
			initView();
		}
	}

	private void initView(){
		mMainView = findViewById(R.id.device_main_view);
		mMainView.setOnClickListener(this);

		mTxtDevName = (TextView)findViewById(R.id.device_name);
		mTxtDevAddr = (TextView)findViewById(R.id.device_address);
		mTxtDevRssi = (TextView)findViewById(R.id.device_rssi);
	}

	public void onClick(View v) {
		AppLog.i(TAG, mDevice.getAddress()+" clicked!");
        if(mClickListener != null){
            mClickListener.onScanDeviceClicked(mDevice);
        }
	}

	public void setClickListener(ScanDeviceClickListener listener){
        mClickListener = listener;
    }

	public void setScannedDevice(ScannedDevice device){
        String name = device.getName();
		mDevice = device;
        if(name == null || name.isEmpty()){
            name = getResources().getString(R.string.device_no_name);
        }
		mTxtDevName.setText(name);
		mTxtDevAddr.setText(device.getAddress());
		mTxtDevRssi.setText(String.valueOf(device.getRssi()));
	}
}
