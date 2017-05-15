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

/**
 * Created by Martin Kern on 12.05.2017.
 */

public class AccService extends Service implements SensorEventListener {
    //Variablen
    private boolean accStatus;
    private double[] acc = {0,0,0};
    private int t, dt;

    //Formalitäten
    private final IBinder mBinder = new AccService.LocalBinder();
    private SensorManager accManager;
    private Sensor accSensor;

    //LocalBinder=======================================================================================
    public class LocalBinder extends Binder {
        public AccService getService() {
            return AccService.this;
        }
    }
//LocalBinder=Ende==================================================================================

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
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onCreate() {
        super.onCreate();
        accManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = accManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public double[] getAcc() {return acc;}
}

