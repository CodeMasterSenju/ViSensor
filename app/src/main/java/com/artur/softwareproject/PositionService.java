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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Martin Kern on 16.05.2017.
 * This service calculates the relative position in cartesian coordinates from GPS position and
 * barometer pressure
 */


public class PositionService extends Service {
    private static final String TAG = PositionService.class.getSimpleName();
    //variables
    private double[] gps = {0,0,0}; // longitude, latitude, height
    private double[] origin = {0,0,0}; // store the point of origin
    private double[] gpsDistance = {0,0,0}; //current position in cartesian coordinates in m

    private double startPressure = 0; //store the pressure when beginning measurement
    private double pressure = 0; //current atmospheric pressure
    private double heightDifference = 0;

    private boolean gpsInit = false; //Has origin been initialized?
    private boolean baroInit = false; //Has startPressure been initialized?

    //formalities
    private Intent gpsIntent, baroIntent;


    private BroadcastReceiver gpsReceive = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            gps = (double[])intent.getExtras().get("gpsRawData");

            if (gps == null)
            {
                Log.d(TAG, "Error while receiving gpsRawData.");

                return;
            }

            if (!gpsInit)
            {
                origin[0] = gps[0];
                origin[1] = gps[1];
                origin[2] = gps[2];

                gpsInit = true;
            }

            //Calculating cartesian coordinates.
            final double mPerLatitude = 111133; //Distance of one degree of latitude.

            gpsDistance[0] = (gps[0] - origin[0]) * mPerLatitude *
                    Math.cos(gps[1] * 2 * Math.PI / 360);

            gpsDistance[1] = (gps[1] - origin[1]) * mPerLatitude;
            gpsDistance[2] = gps[2] - origin[2];

            Intent gpsDistIntent = new Intent();

            gpsDistIntent.putExtra("gpsDistance", gpsDistance);
            gpsDistIntent.setAction("gpsDistFilter");

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsDistIntent);
        }
    };


    private BroadcastReceiver baroReceive = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            pressure = (double)intent.getExtras().get("baroRawData");

            if (!baroInit)
            {
                startPressure = pressure;
                baroInit = true;
            }

            final double mPerPa = 0.11; //11cm height difference per 1Pa.

            heightDifference = (startPressure - pressure) * 100 * mPerPa;

            Intent hDiffIntent = new Intent();

            hDiffIntent.putExtra("hDiff", heightDifference);
            hDiffIntent.setAction("hDiffFilter");

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hDiffIntent);
        }
    };


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d(TAG, "PositionService was created.");

        LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceive,
                new IntentFilter("gpsFilter"));

        LocalBroadcastManager.getInstance(this).registerReceiver(baroReceive,
                new IntentFilter("baroFilter"));

        LocalBroadcastManager.getInstance(this).registerReceiver(resetOrigin,
                new IntentFilter("resetFilter"));

        gpsIntent = new Intent(this, GpsService.class);

        baroIntent = new Intent(this, BaroService.class);

        startService(gpsIntent);
        startService(baroIntent);
    }

    public void onDestroy()
    {
        super.onDestroy();

        stopService(gpsIntent);
        stopService(baroIntent);
    }

    //resets origin
    private BroadcastReceiver resetOrigin = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            origin[0] = gps[0];
            origin[1] = gps[1];
            origin[2] = gps[2];

            gpsDistance[0] = 0;
            gpsDistance[1] = 0;
            gpsDistance[2] = 0;

            startPressure = pressure;

            Intent gpsDistIntent = new Intent();

            gpsDistIntent.putExtra("gpsDistance", gpsDistance);
            gpsDistIntent.setAction("gpsDistFilter");

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsDistIntent);

            Intent hDiffIntent = new Intent();

            hDiffIntent.putExtra("hDiff", heightDifference);
            hDiffIntent.setAction("hDiffFilter");

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hDiffIntent);
        }
    };
}

//EOF