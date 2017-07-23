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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Locale;

/**
 * Created by artur_000 on 01.05.2017.
 * List for displaying sensor data.
 */

class SensorDataListAdapter extends ArrayAdapter<String> {

    private final String[] dataTypes;
    private final String[] dataUnits;

    private static double[] dataValues = {0,0,0,0,0,0,0};

    private double temperature;
    private double humidity;
    private double illuminance;
    private double xPos;
    private double yPos;
    private double zPos;

    private final static String TAG = BluetoothConnectionListAdapter.class.getSimpleName();


    SensorDataListAdapter(Activity context, String[] datenTypen, String[] datenEinheit)
    {
        super(context, R.layout.sensordata_list_pattern, datenTypen);

        this.dataTypes = datenTypen;
        this.dataUnits = datenEinheit;

        temperature = 0;
        humidity = 0;
        xPos = 0;
        yPos = 0;
        zPos = 0;

        BroadcastReceiver temperatureReceive = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                temperature = (double)intent.getExtras().get("ambientTemperature");

            }
        };

        BroadcastReceiver gpsReceive = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                double[] gps = (double[])intent.getExtras().get("gpsDistance");

                if(gps == null)
                {
                    Log.d(TAG,"Error while receiving gpsDistance.");
                    return;
                }

                xPos = gps[0];
                yPos = gps[1];
            }
        };

        BroadcastReceiver baroReceive = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                zPos = (double)intent.getExtras().get("hDiff");
            }
        };

        BroadcastReceiver humidityReceive = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                humidity = (double)intent.getExtras().get("humidity");
            }
        };

        BroadcastReceiver opticalReceive = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                illuminance = (double)intent.getExtras().get("light");
            }
        };

        LocalBroadcastManager.getInstance(context).registerReceiver(temperatureReceive, new IntentFilter("temperatureFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(humidityReceive, new IntentFilter("humidityFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(opticalReceive, new IntentFilter("lightFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(gpsReceive, new IntentFilter("gpsDistFilter"));
        LocalBroadcastManager.getInstance(context).registerReceiver(baroReceive, new IntentFilter("hDiffFilter"));
    }


    private static class SensorViewHolder
    {
        private TextView dataItemName;
        private TextView dataItemType;
        private TextView dataItemValue;
    }


    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {

        SensorViewHolder mViewHolder;

        if (convertView == null)
        {
            mViewHolder = new SensorViewHolder();

            LayoutInflater ListInflater = LayoutInflater.from(getContext());

            convertView = ListInflater.inflate(R.layout.sensordata_list_pattern, parent, false);

            mViewHolder.dataItemName = (TextView) convertView.findViewById(R.id.dataItemName);
            mViewHolder.dataItemType = (TextView) convertView.findViewById(R.id.dataItemType);
            mViewHolder.dataItemValue = (TextView) convertView.findViewById(R.id.dataItemValue);

            convertView.setTag(mViewHolder);
        }
        else
        {
            mViewHolder = (SensorViewHolder) convertView.getTag();
        }


        dataValues[0] = Math.floor(temperature * 100) / 100;
        dataValues[1] = Math.floor(humidity * 100) / 100;
        dataValues[2] = Math.floor(illuminance * 100) / 100;
        dataValues[3] = Math.floor(xPos * 100) / 100;
        dataValues[4] = Math.floor(yPos * 100) / 100;
        dataValues[5] = Math.floor(zPos * 100) / 100;

        if(position == 0 || position == 1 || position == 2)
        {
            mViewHolder.dataItemName.setText(dataTypes[position]);
            mViewHolder.dataItemType.setText(dataUnits[position]);
            mViewHolder.dataItemValue.setText(String.format(Locale.GERMANY, "%1$, .2f",
                    dataValues[position]));//Double.toString(dataValues[position]));
        }
        else if(position == 3)
        {
            mViewHolder.dataItemName.setText(dataTypes[3]);

            mViewHolder.dataItemValue.
                    setText("X : " + Double.toString(dataValues[3]) + " " + dataUnits[3] + "\n" +
                            "Y : " + Double.toString(dataValues[4]) + " " + dataUnits[3] + "\n" +
                            "Z : " + Double.toString(dataValues[5]) + " " + dataUnits[3]);
        }


        return convertView;
    }
}

//EOF