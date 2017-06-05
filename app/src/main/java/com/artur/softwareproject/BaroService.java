package com.artur.softwareproject;

/**
 * Created by Martin Kern on 15.05.2017.
 * This Service gets the data from the barometer.
 */

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BaroService extends Service implements SensorEventListener {
    private static final String TAG = BaroService.class.getSimpleName();

    //air pressure
    private double baro;
    //filter
    private SimpleKalmanFilter filter;

    //formalities
    private SensorManager baroManager;
    private Sensor baroSensor;

//onDestroy=========================================================================================
    @Override
    public void onDestroy() {
        baroManager.unregisterListener(this, baroSensor);
        super.onDestroy();
    }
//onDestroy=End=====================================================================================

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {/*nothing*/}

    public void  onSensorChanged(SensorEvent event) {
        if (event.values.length > 0) {

            baro = filter.output(event.values[0]);

            Intent baroIntent = new Intent();
            baroIntent.putExtra("baroRawData", baro);
            baroIntent.setAction("baroFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(baroIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        baroManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        baroSensor = baroManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        baroManager.registerListener(this, baroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        filter = new SimpleKalmanFilter(0.013, 0.0000005);

    }
}

//EOF