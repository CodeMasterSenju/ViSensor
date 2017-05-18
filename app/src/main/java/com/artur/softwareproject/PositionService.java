package com.artur.softwareproject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Martin Kern on 16.05.2017.
 */

/**
 * Dieser Service ermittelt die Aktuelle Position in karthesischen Koordinaten mit Hilfe von
 * GPS, Beschleunigungssensor und Magnetometer. Folgende Funktionen sind wichtig:
 * "resetUrsprung()":   Setzt, falls GPS verfügbar ist, den Koordinatenursprung.
 *                      Diese Funktion wird aufgerufen, wenn eine Aufzeichnung begonnen wird.
 *
 */

/*Folgender Code muss in jeder Activity vorhanden sein, die diesen Service nutzen möchte

private PositionService mService = null;

private ServiceConnection posConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PositionService.LocalBinder binder = (PositionService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PositionService.class);
        bindService(intent, posConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mService != null) {
            unbindService(posConnection);
            mService = null;
        }
    }

 */

public class PositionService extends Service {
    private static final String TAG = PositionService.class.getSimpleName();
    //Variablen
    private double[] gps = {0,0,0}; // Längengrad, Breitengrad, Höhe
    private double[] ursprung = {0,0,0}; // Wird genutzt um sich die Ursprungsposition zu merken
    private double[] gpsDistanz = {0,0,0}; //Aktuelle Position in karthesischen Koordinaten in m
    private final double mProBreitengrad = 111133; //Abstand zwischen zwei Breitengraden in Metern.

    private double[] acc_1 = {0,0,0}; //Vergangene Messung
    private double[] acc_filter = {0,0,0};
    private double[] acc_2 = {0,0,0,0}; //Aktuelle Messung mit Zeitdifferenz
    private double[] vel = {0,0,0}; //Geschwindigkeit. Berechnet us der Beschleunigung
    private double dt = 0;
    private double[] accDistanz = {0,0,0}; //Position in Karthesischen Koordinaten. Aus der Beschleunigung berechnet.

    //Filter
    private Mittelwertfilter accFX = new Mittelwertfilter(5);
    private Mittelwertfilter accFY = new Mittelwertfilter(5);
    private Mittelwertfilter accFZ = new Mittelwertfilter(5);

    //Formalitäten
    private final IBinder positionBinder = new LocalBinder();
    private Intent gpsIntent, accIntent;

    private BroadcastReceiver gpsReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            gps = (double[])intent.getExtras().get("gpsRawData");

            //Berechne karthesische Koordinaten.
            gpsDistanz[0] = (gps[0] - ursprung[0]) * mProBreitengrad * Math.cos(gps[1]);
            gpsDistanz[1] = (gps[1] - ursprung[1]) * mProBreitengrad;
            gpsDistanz[2] = gps[2];
        }
    };

    private BroadcastReceiver accReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            acc_2 = (double[])intent.getExtras().get("accRawData");

            //Versuch, durch Integration eine Distanz zu berechnen.

            acc_filter[0] = accFX.output(acc_1[0]);
            acc_filter[1] = accFY.output(acc_1[1]);
            acc_filter[2] = accFZ.output(acc_1[2]);

            dt = acc_2[3]/1000000000.0d;




            accDistanz[0] += 0.5d * acc_filter[0] * dt * dt + vel[0] * dt;
            accDistanz[1] += 0.5d * acc_filter[1] * dt * dt + vel[1] * dt;
            accDistanz[2] += 0.5d * acc_filter[2] * dt * dt + vel[2] * dt;

            vel[0] += acc_filter[0] * dt;
            vel[1] += acc_filter[1] * dt;
            vel[2] += acc_filter[2] * dt;

            acc_1[0] = acc_2[0];
            acc_1[1] = acc_2[1];
            acc_1[2] = acc_2[2];
        }
    };

    //LocalBinder=======================================================================================
    public class LocalBinder extends Binder {
        public PositionService getService() {
            return PositionService.this;
        }
    }
//LocalBinder=Ende==================================================================================

    @Override
    public IBinder onBind(Intent intent) {
        return positionBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "PositionService wurde erzeugt.");
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceive, new IntentFilter("gpsFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(accReceive, new IntentFilter("accFilter"));
        gpsIntent = new Intent(this, GpsService.class);
        accIntent = new Intent(this, AccService.class);
        startService(gpsIntent);
        startService(accIntent);

    }

    public void onDestroy() {
        super.onDestroy();
        stopService(gpsIntent);
        stopService(accIntent);
    }

    public double[] getGps() {return gps;}

    public double[] getGpsDistanz() {return gpsDistanz;}

    public double[] getAcc() {return acc_filter;}


    //Setzt den Koordinatenursprung
    public void resetUrsprung() {
        ursprung[0] = gps[0];
        ursprung[1] = gps[1];
        ursprung[2] = gps[2];

        accDistanz[0] = 0;
        accDistanz[1] = 0;
        accDistanz[2] = 0;

        gpsDistanz[0] = 0;
        gpsDistanz[1] = 0;
        gpsDistanz[2] = 0;

        vel[0] = 0;
        vel[1] = 0;
        vel[2] = 0;
    }
}
