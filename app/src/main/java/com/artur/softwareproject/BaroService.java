package com.artur.softwareproject;

/**
 * Created by Martin Kern on 15.05.2017.
 * Dieser Service holt sich die Messdaten des Barometers ab.
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

public class BaroService extends Service implements SensorEventListener {
    private static final String TAG = BaroService.class.getSimpleName();
    //Variablen
    private boolean baroStatus;
    //Luftdruck
    private double baro;

    //Formalitäten
    private SensorManager baroManager;
    private Sensor baroSensor;

    //onDestroy=========================================================================================
    @Override
    public void onDestroy() {
        baroManager.unregisterListener(this, baroSensor);
        super.onDestroy();
    }
//onDestroy=Ende====================================================================================

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {/*nichts*/}

    public void  onSensorChanged(SensorEvent event) {
        if (event.values.length > 0) {

            baro = event.values[0];

            Intent gpsIntent = new Intent();
            gpsIntent.putExtra("baroRawData", baro);
            gpsIntent.setAction("baroFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsIntent);
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
    }
}

