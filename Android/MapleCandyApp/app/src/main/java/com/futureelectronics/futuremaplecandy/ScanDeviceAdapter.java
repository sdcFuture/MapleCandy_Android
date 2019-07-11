package com.futureelectronics.futuremaplecandy;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.futureelectronics.futuremaplecandy.scan.ScannedDevice;
import com.futureelectronics.futuremaplecandy.views.ScanDeviceView;

import java.util.List;

/**
 * Created by Kyle.Harman on 9/27/2017.
 */

public class ScanDeviceAdapter extends RecyclerView.Adapter<ScanDeviceAdapter.ScanDeviceViewHolder>{
    private List<ScannedDevice> mDevices;
    private ScanDeviceView.ScanDeviceClickListener mClickListener;

    public static class ScanDeviceViewHolder extends RecyclerView.ViewHolder{
        public ScanDeviceView scanDeviceView;

        public ScanDeviceViewHolder(ScanDeviceView itemView){
            super(itemView);

            scanDeviceView = itemView;
        }
    }

    public ScanDeviceAdapter(ScanDeviceView.ScanDeviceClickListener listener){
        mClickListener = listener;
    }

    @Override
    public ScanDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        ScanDeviceView sdv = (ScanDeviceView) LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_scandevice, parent, false);
        sdv.setClickListener(mClickListener);

        ScanDeviceViewHolder sdvh = new ScanDeviceViewHolder(sdv);
        return sdvh;
    }

    @Override
    public void onBindViewHolder(ScanDeviceViewHolder sdvh, int pos){
        if(mDevices != null && mDevices.size() > pos){
            sdvh.scanDeviceView.setScannedDevice(mDevices.get(pos));
        }
    }

    @Override
    public int getItemCount(){
        return mDevices != null ? mDevices.size() : 0;
    }

    public void setScannedDevices(List<ScannedDevice> devices){
        mDevices = devices;
        notifyDataSetChanged();
    }
}
