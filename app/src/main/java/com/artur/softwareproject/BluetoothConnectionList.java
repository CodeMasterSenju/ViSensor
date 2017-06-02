package com.artur.softwareproject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.os.Handler;
import android.bluetooth.*;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by artur_000 on 01.05.2017.
 * Liste um verfügbare Bluetooth-Geräte anzuzeigen.
 */

public class BluetoothConnectionList extends AppCompatActivity{

    private ListView bluetoothList;
    private ListAdapter ListAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private ArrayList<String> bluetoothName = new ArrayList<>();
    private ArrayList<String> bluetoothAddress = new ArrayList<>();
    private ArrayList<BluetoothDevice> bDevices = new ArrayList<>();

    private static final long SCAN_PERIOD = 5000;
    private final int REQUEST_ENABLE_BT = 1;

    private File topLevelDir;
    private File jsonDir;
    private File objDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);

        bluetoothList = (ListView) findViewById(R.id.bluetoothList);
        ListAdapter = new BluetoothConnectionListAdapter(this, bluetoothAddress, bluetoothName, bDevices);
        bluetoothList.setAdapter(ListAdapter);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();

        //Creating the directory structure for the app.
        topLevelDir = new File(Environment.getExternalStorageDirectory() + "/ViSensor");
        jsonDir = new File(Environment.getExternalStorageDirectory() + "/ViSensor/Json");
        objDir = new File(Environment.getExternalStorageDirectory() + "/ViSensor/Obj");

        if(!topLevelDir.exists()) {
            topLevelDir.mkdir();

            if (!jsonDir.exists()) {
                jsonDir.mkdir();
            }

            if (!objDir.exists()) {
                objDir.mkdir();
            }
        } else if(!jsonDir.exists()) {
            jsonDir.mkdir();

            if(!objDir.exists()) {
                objDir.mkdir();
            }
        } else if(!objDir.exists()) {
            objDir.mkdir();
        }


        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        int permissionCheckLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheckWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissionCheckLocation != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if(permissionCheckWrite != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        scanLeDevice(true);
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!bDevices.contains(device))
                            {
                                bluetoothAddress.add(device.getAddress());
                                bluetoothName.add(device.getName());
                                bDevices.add(device);
                                device.getBondState();
                                ((BaseAdapter)ListAdapter).notifyDataSetChanged();
                            }
                        }
                    });
                }
            };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

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
                bluetoothName.clear();
                bluetoothAddress.clear();
                bDevices.clear();
                ((BaseAdapter)ListAdapter).notifyDataSetChanged();

                if(mScanning == false)
                {
                    scanLeDevice(true);
                }
                else
                {
                    Context context = getApplicationContext();
                    CharSequence text = "Scanning still in process";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return true;

            case R.id.vr_menu_start:
                Intent vrIntent = new Intent(BluetoothConnectionList.this, VRmenu.class);
                BluetoothConnectionList.this.startActivity(vrIntent);

                return true;

                return true;

            case R.id.vr_menu_start:
                Intent vrIntent = new Intent(this, VRmenu.class);
                BluetoothConnectionList.this.startActivity(vrIntent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}