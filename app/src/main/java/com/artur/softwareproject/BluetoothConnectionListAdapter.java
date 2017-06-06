package com.artur.softwareproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static android.os.SystemClock.sleep;

/**
 * Created by artur_000 on 03.05.2017.
 * This is a list of available bluetooth devices.
 */

public class BluetoothConnectionListAdapter extends ArrayAdapter {

    private ArrayList<String> bluetoothAddress;
    private ArrayList<String> bluetoothName;
    private ArrayList<BluetoothDevice> bDevices;
    private Activity contextActivity;
    private Intent intent;
    private Intent mainIntent;
    private int connected;
    private Thread connectThread;

    public TextView bluetoothConnectionName;
    public TextView bluetoothConnectionStatus;

    private static final String TAG = PositionService.class.getSimpleName();

    public BluetoothConnectionListAdapter(Activity context, ArrayList<String> bluetoothAddress, ArrayList<String> bluetoothName, ArrayList<BluetoothDevice> bDevices){
        super(context, R.layout.activity_bluetooth_connection, bluetoothAddress);
        this.bluetoothAddress = bluetoothAddress;
        this.bluetoothName = bluetoothName;
        this.bDevices = bDevices;
        this.contextActivity = context;
        connected = 0;
        this.intent = new Intent();

        LocalBroadcastManager.getInstance(context).registerReceiver(connectedReceive, new IntentFilter("connectedFilter"));
    }

    private BroadcastReceiver connectedReceive = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            connected = (int)intent.getExtras().get("connected");
            Log.d(TAG, "RECEIVE : " + connected);
        }
    };

    public int getConnected()
    {
        return connected;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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
                Animation animation = new AlphaAnimation(0.3f, 1.0f);
                animation.setDuration(1000);
                v.startAnimation(animation);

                intent = new Intent(contextActivity, BluetoothService.class);
                intent.putExtra("device", bDevices.get(position));
                intent.putExtra("deviceList", bDevices);
                contextActivity.startService(intent);

                final ProgressDialog connectingDialog = new ProgressDialog(contextActivity);
                connectingDialog.setMessage("Connecting...");
                connectingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                connectingDialog.show();

                final Handler handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        Log.d(TAG, "MESSAGE RECEIVED");
                        connectingDialog.dismiss();
                        mainIntent = new Intent(contextActivity, Main.class);
                        contextActivity.startActivity(mainIntent);
                        connectThread.interrupt();
                        connected = 0;
                        Log.d(TAG, "THREAD INTERRUPTED " + connectThread.getState());
                        contextActivity.finish();
                    }
                };

                connectThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int stop = 0;
                        while(stop == 0) {
                            stop = getConnected();
                            sleep(2000);
                        }
                        handler.sendEmptyMessage(0);
                    }
                });

                connectThread.start();
            }
        });

        return customView;
    }
}

//EOF