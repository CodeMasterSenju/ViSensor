package com.artur.softwareproject;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.bluetooth.*;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by artur_000 on 01.05.2017.
 */

public class BluetoothConnection extends AppCompatActivity{

    private ListView bluetoothList;
    private ListAdapter adapter;
    private BluetoothAdapter bluetooth;
    private Set<BluetoothDevice> pairedDevices;

    ArrayList<String> bluetoothName = new ArrayList<String>();
    ArrayList<String> bluetoothAddress = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //implements the back button (android handles that by default)
        setContentView(R.layout.activity_bluetooth_connection);

        bluetoothList = (ListView) findViewById(R.id.bluetoothList);
        adapter = new BluetoothConnectionListAdapter(this, bluetoothAddress, bluetoothName);
        bluetoothList.setAdapter(adapter);

        bluetooth = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver,filter);

        if(bluetooth != null)
        {
            if(bluetooth.isEnabled())
            {
                ActivityCompat.requestPermissions(BluetoothConnection.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 90);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},99);
                bluetooth.startDiscovery();
            }
            else
            {
                Context context = getApplicationContext();
                CharSequence text = "Please enable bluetooth";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothAddress.add(device.getAddress());

                if(device.getName() != null)
                {
                    //Hier ist ein kommentar.
                    bluetoothName.add(device.getName());
                }
                else
                    bluetoothName.add("Device name not available");

                ((BaseAdapter)adapter).notifyDataSetChanged();

                System.out.println(device.getAddress());
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.bluetooth_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;

            case R.id.refresh:
                //This space is for the functionality of the bluetooth refresh menu button.

                bluetooth = BluetoothAdapter.getDefaultAdapter();

                if(bluetooth != null)
                {
                    if(bluetooth.isEnabled())
                    {
                        bluetoothAddress.clear();
                        bluetoothName.clear();
                        ((BaseAdapter)adapter).notifyDataSetChanged();

                        if (bluetooth.isDiscovering()) {
                            // Bluetooth is already in modo discovery mode, we cancel to restart it again
                            bluetooth.cancelDiscovery();
                        }

                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},90);
                        ActivityCompat.requestPermissions(BluetoothConnection.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 99);
                        bluetooth.startDiscovery();
                    }
                    else
                    {
                        Context context = getApplicationContext();
                        CharSequence text = "Please enable bluetooth";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }

                Context context = getApplicationContext();
                CharSequence text = "Refreshing connections";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}