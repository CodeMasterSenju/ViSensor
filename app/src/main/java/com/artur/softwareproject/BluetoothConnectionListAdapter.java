package com.artur.softwareproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by artur_000 on 03.05.2017.
 */

public class BluetoothConnectionListAdapter extends ArrayAdapter {

    private final ArrayList<String> bluetoothAddress;
    private final ArrayList<String> bluetoothName;

    public TextView bluetoothConnectionName;
    public TextView bluetoothConnectionStatus;

    public BluetoothConnectionListAdapter(Activity context, ArrayList<String> bluetoothAddress, ArrayList<String> bluetoothName){
        super(context, R.layout.activity_bluetooth_connection, bluetoothAddress);
        this.bluetoothAddress = bluetoothAddress;
        this.bluetoothName = bluetoothName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater ListInflater = LayoutInflater.from(getContext());
        View customView = ListInflater.inflate(R.layout.bluetooth_list_pattern, parent, false);

        bluetoothConnectionName = (TextView) customView.findViewById(R.id.bluetooth_connection_name);
        bluetoothConnectionName.setText(bluetoothAddress.get(position));

        bluetoothConnectionStatus = (TextView) customView.findViewById(R.id.bluetooth_connection_status);
        bluetoothConnectionStatus.setText(bluetoothName.get(position));

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What happens if you press on the list items at the bluetooth activity.
                CharSequence text = "Connecting to bluetooth device";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(v.getContext(), text, duration);
                toast.show();
            }
        });

        return customView;
    }
}