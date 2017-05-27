package com.artur.softwareproject;

/**
 * Created by artur_000 on 01.05.2017.
 * Liste zur Darstellung von Sensordaten
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.*;

public class SensorDataListAdapter extends ArrayAdapter {

    private final Activity context;
    private final String[] datenTypen;
    private final String[] datenEinheit;
    private static double[] dataValues = {0,0,0,0,0,0,0};
    private TextView dataItemValue;
    private TextView dataItemName;
    private TextView dataItemType;
    private JsonData json = new JsonData();

    private final static String TAG = BluetoothConnectionListAdapter.class.getSimpleName();

    public class JsonData {//Daten kommen in eine Klasse um später in einer JSON-Datei gespeichert zu werden.

        public JsonData() {
            temperature = 0;
            humidity = 0;
            illuminance = 0;
            xPos = 0;
            yPos = 0;
            zPos = 0;
        }

        public double temperature;
        public double humidity;
        public double illuminance;
        public double xPos;
        public double yPos;
        public double zPos;
    }


    public SensorDataListAdapter(Activity context, String[] datenTypen, String[] datenEinheit) {
        super(context, R.layout.sensordata_list_pattern, datenTypen);
        this.context = context;
        this.datenTypen = datenTypen;
        this.datenEinheit = datenEinheit;
        LocalBroadcastManager.getInstance(context).registerReceiver(temperatureReceive, new IntentFilter("temperatureFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(humidityReceive, new IntentFilter("humidityFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(opticalReceive, new IntentFilter("lightFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(gpsReceive, new IntentFilter("gpsDistFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(baroReceive, new IntentFilter("hDiffFilter"));

    }

    private BroadcastReceiver temperatureReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            json.temperature = (double)intent.getExtras().get("ambientTemperature");

        }
    };

    private BroadcastReceiver gpsReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] gps = (double[])intent.getExtras().get("gpsDistanz");
            json.xPos = gps[0];
            json.yPos = gps[1];
        }
    };

    private BroadcastReceiver baroReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            json.zPos = (double)intent.getExtras().get("hDiff");
        }
    };

    private BroadcastReceiver humidityReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            json.humidity = (double)intent.getExtras().get("humidity");
        }
    };

    private BroadcastReceiver opticalReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            json.illuminance = (double)intent.getExtras().get("light");
        }
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater ListInflater = LayoutInflater.from(getContext());
        View customView = ListInflater.inflate(R.layout.sensordata_list_pattern, parent, false);

        dataItemName = (TextView) customView.findViewById(R.id.dataItemName);
        dataItemName.setText(datenTypen[position]);

        dataItemType = (TextView) customView.findViewById(R.id.dataItemType);
        dataItemType.setText(datenEinheit[position]);

        dataItemValue = (TextView) customView.findViewById(R.id.dataItemValue);

        dataValues[0] = Math.floor(json.temperature * 100) / 100;
        dataValues[1] = Math.floor(json.humidity * 100) / 100;
        dataValues[2] = Math.floor(json.illuminance * 100) / 100;
        dataValues[3] = Math.floor(json.xPos * 100) / 100;
        dataValues[4] = Math.floor(json.yPos * 100) / 100;
        dataValues[5] = Math.floor(json.zPos * 100) / 100;

        dataItemValue.setText(Double.toString(dataValues[position]));


        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What happens if you press on the list items at the main activity.
                Intent vrIntent = new Intent(v.getContext(), VRmenu.class);
                v.getContext().startActivity(vrIntent);
            }
        });

        return customView;
    }

    public JsonData getJson() {
        return json;
    }

}