package com.artur.softwareproject;

/**
 * Created by Martin Kern on 15.05.2017.
 */

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Martin Kern on 12.05.2017.
 */

/**
 * Dieser Service holt sich die Messdaten des Beschleunigungssensors ab
 * und bestimmt das Zeitintervall zwischen Messungen.
 */

public class AccService extends Service implements SensorEventListener {
    private static final String TAG = AccService.class.getSimpleName();
    //Variablen
    private boolean accStatus;
    //Beschleunigung in x-, y- und z-Richtung. Der vierte Eintrag ist die Zeitdifferenz zwichen Messungen.
    private double[] acc = {0,0,0,0};
    private long t_1, t_2; //Variablen zur berechnung der Zeitdifferenz.

    //Formalitäten
    private SensorManager accManager;
    private Sensor accSensor;

    //onDestroy=========================================================================================
    @Override
    public void onDestroy() {
        accManager.unregisterListener(this, accSensor);
        super.onDestroy();
    }
//onDestroy=Ende====================================================================================

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {/*nichts*/}

    public void  onSensorChanged(SensorEvent event) {
        if (event.values.length > 0) {
            for (int i = 0; i < 3; i++) {
                acc[i] = event.values[i];
            }

            t_2 = SystemClock.elapsedRealtimeNanos();
            acc[3] = (double) (t_2 - t_1);
            t_1 = t_2;

            Intent gpsIntent = new Intent();
            gpsIntent.putExtra("accRawData", acc);
            gpsIntent.setAction("accFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        accManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = accManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
        t_1 = SystemClock.elapsedRealtimeNanos();
    }
}

