/* Copyright 2017 Artur Baltabayev, Jean Büsche, Martin Kern, Gabriel Scheibler
 *
 * This file is part of ViSensor.
 *
 * ViSensor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViSensor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViSensor.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.artur.softwareproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.os.SystemClock.sleep;

/**
 * Created by artur_000 on 03.05.2017.
 * This is a list of available bluetooth devices.
 */

class BluetoothConnectionListAdapter extends ArrayAdapter<String>
{
    private static final String TAG = BluetoothConnectionListAdapter.class.getSimpleName();

    private ArrayList<String> bluetoothAddress;
    private ArrayList<String> bluetoothName;
    private ArrayList<BluetoothDevice> bDevices;

    private Activity contextActivity;

    private Intent intent;
    private Intent mainIntent;
    private final Intent bluetoothServiceIntent;

    private int connected;

    private Thread connectThread;

    private ProgressDialog connectDialog;

    private boolean timeout;

    private final BluetoothConnectionListAdapter bclaReference = this;


    BluetoothConnectionListAdapter(Activity context,
                                   ArrayList<String> bluetoothAddress,
                                   ArrayList<String> bluetoothName,
                                   ArrayList<BluetoothDevice> bDevices)
    {

        super(context, R.layout.activity_bluetooth_connection, bluetoothAddress);

        this.bluetoothAddress = bluetoothAddress;
        this.bluetoothName = bluetoothName;
        this.bDevices = bDevices;
        this.contextActivity = context;
        this.intent = new Intent();

        connected = 0;

        timeout = false;

        bluetoothServiceIntent = new Intent(context, BluetoothService.class);

        BroadcastReceiver connectedReceive = new BroadcastReceiver()
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {

                connected = (int)intent.getExtras().get("connected");

                Log.d(TAG, "RECEIVE : " + connected);
            }
        };

        LocalBroadcastManager.getInstance(context).registerReceiver(connectedReceive, new IntentFilter("connectedFilter"));
    }


    private int getConnected()
    {
        return connected;
    }


    private static class ViewHolder {

        private TextView bluetoothConnectionName;

        private TextView bluetoothConnectionStatus;
    }


    private static class ConnectionHandlerClass extends Handler
    {
        private final WeakReference<BluetoothConnectionListAdapter> mTarget;

        ConnectionHandlerClass(BluetoothConnectionListAdapter context)
        {
            mTarget = new WeakReference<>(context);
        }

        @Override
        public void handleMessage (Message msg)
        {
            super.handleMessage(msg);

            BluetoothConnectionListAdapter target = mTarget.get();

            target.connectDialog.dismiss();

            if (target.timeout)
            {
                target.contextActivity.stopService(target.bluetoothServiceIntent); //The BluetoothService needs to be stopped if connecting timed out.

                target.timeout = false; //Reset timeout variable to try connecting again.
            }
            else
            {
                target.mainIntent = new Intent(target.contextActivity, Main.class);

                target.contextActivity.startActivity(target.mainIntent);

                target.connectThread.interrupt();

                target.connected = 0;

                Log.d(TAG, "THREAD INTERRUPTED " + target.connectThread.getState());

                target.contextActivity.finish();
            }
        }
    }


    @Override @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent)
    {

        ViewHolder mViewHolder;

        if (convertView == null)
        {
            mViewHolder = new ViewHolder();

            LayoutInflater ListInflater = LayoutInflater.from(getContext());

            convertView = ListInflater.inflate(R.layout.bluetooth_list_pattern, parent, false);

            mViewHolder.bluetoothConnectionName = (TextView) convertView.findViewById(R.id.bluetooth_connection_name);
            mViewHolder.bluetoothConnectionStatus = (TextView) convertView.findViewById(R.id.bluetooth_connection_status);

            convertView.setTag(mViewHolder);

            //final ConnectionHandlerClass connectHandler = new ConnectionHandlerClass(this);

            convertView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    //What happens if you press on the list items at the bluetooth activity.
                    Animation animation = new AlphaAnimation(0.3f, 1.0f);

                    animation.setDuration(1000);

                    v.startAnimation(animation);

                    intent = new Intent(contextActivity, BluetoothService.class);

                    intent.putExtra("device", bDevices.get(position));
                    intent.putExtra("deviceList", bDevices);

                    contextActivity.startService(intent);

                    final ProgressDialog connectingDialog = new ProgressDialog(contextActivity);

                    connectDialog = connectingDialog;

                    connectingDialog.setMessage("Connecting...");
                    connectingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    connectingDialog.setCancelable(false);
                    connectingDialog.show();

                    final ConnectionHandlerClass connectHandler = new ConnectionHandlerClass(bclaReference);

                    connectThread = new Thread(new Runnable()
                    {
                        @Override
                        public void run() {

                            int stop = 0;
                            int counter = 0;

                            //Timeout after 10 seconds.
                            while (stop == 0 && counter < 5)
                            {
                                stop = getConnected();
                                counter++;
                                sleep(2000);
                            }

                            if (stop == 0 && counter == 4)//Timeout occurred after 10s of waiting and stop is still 0.
                            {
                                timeout = true;
                            }

                            connectHandler.sendEmptyMessage(0);
                        }
                    });

                    connectThread.start();
                }
            });
        }
        else
        {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.bluetoothConnectionName.setText(bluetoothAddress.get(position));
        mViewHolder.bluetoothConnectionStatus.setText(bluetoothName.get(position));

        return convertView;
    }
}

//EOF