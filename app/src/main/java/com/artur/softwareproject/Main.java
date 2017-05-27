package com.artur.softwareproject;

/**
 * Created by artur_000 on 01.05.2017.
 * Zeigt Sensordaten in Echtzeit an.
 */

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;


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

    private boolean record = false;
    private boolean firstWrite = true;
    private String fileName;

    Handler updateHandler = new Handler();
    Handler recordHandler = new Handler();

    private File jsonFile;


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
        recordHandler.postDelayed(jsonDocWriter, 500);
        super.onResume();
    }

    @Override
    protected void onPause() {
        updateHandler.removeCallbacks(timerRunnable);
        recordHandler.removeCallbacks(jsonDocWriter);
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
                CharSequence text = "Recording data";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }




    public void record(View view) {

        firstWrite = true;
        Intent resetIntent = new Intent();
        resetIntent.putExtra("reset", "");
        resetIntent.setAction("resetFilter");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resetIntent);

        if (record) {
            record = false;

            try {
                Log.d(TAG, "trying to finish file");
                BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
                writer.write("\n]}");
                writer.close();
            } catch (IOException e) {
                Log.d(TAG, "writing failed");
                e.printStackTrace();
            }
        } else {
            record = true;

            fileName = now() + ".json";

            jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

            try {
                jsonFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
                writer.write("{\"session\": [\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    Runnable jsonDocWriter = new Runnable() {
        @Override
        public void run() {
            if (record) {
                String string = dataToJson();
                if (string == "" || string == null)
                    string = "Nya";

                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
                    Log.d(TAG, "writing to JSON-file.");
                    if (firstWrite) {
                        writer.write(string);
                        firstWrite = false;
                    } else {
                        writer.write(",\n" + string);
                    }

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            recordHandler.postDelayed(this, 1000); //run every second
        }
    };

    private String dataToJson() {
        SensorDataListAdapter.JsonData data = adapter.getJson();

        String ret =    "  {\n"
                        + "    \"time\": " + now() + ",\n"
                        + "    \"temperature\": " + Double.toString(data.temperature) + ",\n"
                        + "    \"humidity\": " + Double.toString(data.humidity) + ",\n"
                        + "    \"illuminance\": " + Double.toString(data.illuminance) + ",\n"
                        + "    \"xPos\": " + Double.toString(data.xPos) + ",\n"
                        + "    \"yPos\": " + Double.toString(data.yPos) + ",\n"
                        + "    \"zPos\": " + Double.toString(data.zPos) + "\n"
                        + "  }";

        return ret;
    }

    //Gibt die aktuelle Zeit als String aus
    private String now() {
        GregorianCalendar now = new GregorianCalendar();
        String ret;

        ret = ""   + now.get(Calendar.YEAR)
                + "-" + now.get(Calendar.MONTH)
                + "-" + now.get(Calendar.DAY_OF_MONTH)
                + "-" + now.get(Calendar.HOUR)
                + "-" + now.get(Calendar.MINUTE)
                + "-" + now.get(Calendar.SECOND);

        return ret;
    }


}