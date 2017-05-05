package com.artur.softwareproject;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by artur_000 on 03.05.2017.
 */

public class BluetoothConnectionListAdapter extends ArrayAdapter {

    private final ArrayList<String> bluetoothAddress;
    private final ArrayList<String> bluetoothName;
    private final ArrayList<BluetoothDevice> bDevices;
    private int on;

    public TextView bluetoothConnectionName;
    public TextView bluetoothConnectionStatus;
    public ImageView bluetoothConnectionImage;

    private static final UUID myUUID = UUID.fromString("304a7cce-3120-11e7-93ae-92361f002671");
    BluetoothConnection con;

    public BluetoothConnectionListAdapter(Activity context, ArrayList<String> bluetoothAddress, ArrayList<String> bluetoothName, ArrayList<BluetoothDevice> bDevices){
        super(context, R.layout.activity_bluetooth_connection, bluetoothAddress);
        this.bluetoothAddress = bluetoothAddress;
        this.bluetoothName = bluetoothName;
        this.bDevices = bDevices;
        on = 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater ListInflater = LayoutInflater.from(getContext());
        View customView = ListInflater.inflate(R.layout.bluetooth_list_pattern, parent, false);

        bluetoothConnectionImage = (ImageView) customView.findViewById(R.id.bluetooth_connected_image);

        bluetoothConnectionName = (TextView) customView.findViewById(R.id.bluetooth_connection_name);
        bluetoothConnectionName.setText(bluetoothAddress.get(position));

        bluetoothConnectionStatus = (TextView) customView.findViewById(R.id.bluetooth_connection_status);
        bluetoothConnectionStatus.setText(bluetoothName.get(position));

        con = new BluetoothConnection(getContext());

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What happens if you press on the list items at the bluetooth activity.

                if(on == 0)
                {
                    con.startClient(bDevices.get(position), myUUID);
                    bluetoothConnectionImage.setBackgroundResource(R.drawable.ic_action_bluetooth_connected);

                    CharSequence text = "Connecting to bluetooth device " + bDevices.get(position).getName();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(v.getContext(), text, duration);
                    toast.show();

                    on = 1;
                } else if (on == 1) {
                    con.stop();
                    bluetoothConnectionImage.setBackgroundResource(0);

                    CharSequence text = "Disconnecting from bluetooth device " + bDevices.get(position).getName();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(v.getContext(), text, duration);
                    toast.show();

                    on = 0;
                }
            }
        });

        return customView;
    }


}