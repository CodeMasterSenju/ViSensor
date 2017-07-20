/* Copyright 2017 Artur Baltabayev, Jean-Josef Büschel, Martin Kern, Gabriel Scheibler
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
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Martin Kern on 15.05.2017.
 * This Service gets the data from the barometer.
 */

public class BaroService extends Service implements SensorEventListener
{

    //air pressure
    private double baro;
    //filter
    private SimpleKalmanFilter filter;
    //formalities
    private SensorManager baroManager;
    private Sensor baroSensor;


    @Override
    public void onDestroy()
    {
        baroManager.unregisterListener(this, baroSensor);

        super.onDestroy();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {/*nothing*/}


    public void onSensorChanged(SensorEvent event)
    {
        if (event.values.length > 0)
        {

            baro = filter.output(event.values[0]);

            Intent baroIntent = new Intent();
            baroIntent.putExtra("baroRawData", baro);
            baroIntent.setAction("baroFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(baroIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();

        baroManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        baroSensor = baroManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        baroManager.registerListener(this, baroSensor, SensorManager.SENSOR_DELAY_FASTEST);

        filter = new SimpleKalmanFilter(0.013, 0.0000005);
    }
}

//EOF