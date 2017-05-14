package com.artur.softwareproject;

/**
 * Created by artur_000 on 01.05.2017.
 */

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Main extends AppCompatActivity {

    public String [] datenTypen = {"Temperatur", "Luftfeuchtigkeit","Helligkeit"};
    static public String [] datenEinheit = {"°C", "%", "lx"};
    private ListView sensorDataList;
    private ListAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private Intent serviceIntent;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false); /* Gets rid of the action bar title. */
        setContentView(R.layout.activity_main);

        sensorDataList = (ListView) findViewById(R.id.dataList);
        adapter = new SensorDataListAdapter(this, datenTypen, datenEinheit);
        sensorDataList.setAdapter(adapter);
        serviceIntent = new Intent(this, BluetoothService.class);

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            ((BaseAdapter)adapter).notifyDataSetChanged();
            handler.postDelayed(this, 1000); //run every second
        }
    };

    @Override
    protected void onResume() {
        handler.postDelayed(timerRunnable, 500);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(timerRunnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_geräte:
                Intent bluetoothIntent = new Intent(Main.this, BluetoothConnectionList.class);
                Main.this.startActivity(bluetoothIntent);
                return true;

            case R.id.menu_hilfe:
                Intent helpIntent = new Intent(Main.this, Help.class);
                Main.this.startActivity(helpIntent);
                return true;

            case R.id.record_data:

                Context context = getApplicationContext();
                CharSequence text = "Recording data";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}