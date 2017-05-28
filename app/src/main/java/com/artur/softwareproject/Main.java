package com.artur.softwareproject;

/**
 * Created by artur_000 on 01.05.2017.
 * Zeigt Sensordaten in Echtzeit an.
 */

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;



public class Main extends AppCompatActivity {

    private static final String TAG = Main.class.getSimpleName();

    public String [] datenTypen = {"Temperatur", "Luftfeuchtigkeit","Helligkeit","GPS x", "GPS y", "Baro z"};
    static public String [] datenEinheit = {"°C", "%", "lx", "m", "m", "m"};
    private ListView sensorDataList;
    private SensorDataListAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private Intent serviceIntent;
    private Intent posServiceIntent;
    private Intent recordServiceIntent;

    private boolean record = false;

    Handler updateHandler = new Handler();



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

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        posServiceIntent = new Intent(this, PositionService.class);
        startService(posServiceIntent);

        recordServiceIntent = new Intent(this, RecordService.class);

    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            ((BaseAdapter)adapter).notifyDataSetChanged();
            updateHandler.postDelayed(this, 1000); //run every second
        }
    };

    @Override
    protected void onResume() {
        updateHandler.postDelayed(timerRunnable, 500);
        super.onResume();
    }

    @Override
    protected void onPause() {
        updateHandler.removeCallbacks(timerRunnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(serviceIntent);
        stopService(posServiceIntent);
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
                record();
                CharSequence text = "Recording data";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void record() {

        Intent resetIntent = new Intent();
        resetIntent.putExtra("reset", "");
        resetIntent.setAction("resetFilter");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resetIntent);

        if (!record) {
            record = true;
            startService(recordServiceIntent);

        } else {
            record = false;
            stopService(recordServiceIntent);
        }
    }

}

//EOF