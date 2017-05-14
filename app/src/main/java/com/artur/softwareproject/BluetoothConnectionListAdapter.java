package com.artur.softwareproject;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by artur_000 on 03.05.2017.
 */

public class BluetoothConnectionListAdapter extends ArrayAdapter {

    private ArrayList<String> bluetoothAddress;
    private ArrayList<String> bluetoothName;
    private ArrayList<BluetoothDevice> bDevices;
    private Activity contextActivity;
    private Intent intent;

    public TextView bluetoothConnectionName;
    public TextView bluetoothConnectionStatus;
    public ImageView bluetoothConnectionImage;
    public ProgressBar bluetoothConnectionProgress;


    public BluetoothConnectionListAdapter(Activity context, ArrayList<String> bluetoothAddress, ArrayList<String> bluetoothName, ArrayList<BluetoothDevice> bDevices){
        super(context, R.layout.activity_bluetooth_connection, bluetoothAddress);
        this.bluetoothAddress = bluetoothAddress;
        this.bluetoothName = bluetoothName;
        this.bDevices = bDevices;
        this.contextActivity = context;
        this.intent = new Intent();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater ListInflater = LayoutInflater.from(getContext());
        View customView = ListInflater.inflate(R.layout.bluetooth_list_pattern, parent, false);

        //bluetoothConnectionProgress = (ProgressBar) customView.findViewById(R.id.loadingAnim);

        bluetoothConnectionName = (TextView) customView.findViewById(R.id.bluetooth_connection_name);
        bluetoothConnectionName.setText(bluetoothAddress.get(position));

        bluetoothConnectionStatus = (TextView) customView.findViewById(R.id.bluetooth_connection_status);
        bluetoothConnectionStatus.setText(bluetoothName.get(position));

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What happens if you press on the list items at the bluetooth activity.
                intent = new Intent(contextActivity, BluetoothService.class);
                intent.putExtra("device", bDevices.get(position));
                intent.putExtra("deviceList", bDevices);
                contextActivity.startService(intent);
            }
        });

        return customView;
    }
}