package com.artur.softwareproject;

/**
 * Created by artur_000 on 01.05.2017.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static double[] dataValues = {0,0,0,0};
    private TextView dataItemValue;
    private TextView dataItemName;
    private TextView dataItemType;
    private double tempValue;
    private double humValue;
    private double lightValue;

    private final static String TAG = BluetoothConnectionListAdapter.class.getSimpleName();

    public SensorDataListAdapter(Activity context, String[] datenTypen, String[] datenEinheit) {
        super(context, R.layout.sensordata_list_pattern, datenTypen);
        this.context = context;
        this.datenTypen = datenTypen;
        this.datenEinheit = datenEinheit;
        LocalBroadcastManager.getInstance(context).registerReceiver(temperatureReceive, new IntentFilter("temperatureFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(humidityReceive, new IntentFilter("humidityFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(opticalReceive, new IntentFilter("lightFilter"));
    }

    private BroadcastReceiver temperatureReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tempValue = (double)intent.getExtras().get("ambientTemperature");
        }
    };

    private BroadcastReceiver humidityReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            humValue = (double)intent.getExtras().get("humidity");
        }
    };

    private BroadcastReceiver opticalReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            lightValue = (double)intent.getExtras().get("light");
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

        dataValues[0] = Math.floor(tempValue * 100) / 100;
        dataValues[1] = Math.floor(humValue * 100) / 100;
        dataValues[2] = Math.floor(lightValue * 100) / 100;

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
}