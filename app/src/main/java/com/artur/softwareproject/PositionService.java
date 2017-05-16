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

    private double[] acc = {0,0,0,0}; //Beschleunigungssensor mit Zeitdifferenz
    private double[] mag = {0,0,0}; //Magnetometer (Kompass, Hall-Sonde)
    private double[] vel = {0,0,0}; //Geschwindigkeit als Integral der Beschleunigung
    private double[] accDistanz = {0,0,0}; //Position in Karthesischen Koordinaten. Aus der Beschleunigung berechnet.

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
            acc = (double[])intent.getExtras().get("accRawData");
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

    public double[] getAcc() {return acc;}


    //Setzt den Koordinatenursprung
    public void resetUrsprung() {
        ursprung[0] = gps[0];
        ursprung[1] = gps[1];
        ursprung[2] = gps[2];

        gpsDistanz[0] = (gps[0] - ursprung[0]) * mProBreitengrad * Math.cos(gps[1]);
        gpsDistanz[1] = (gps[1] - ursprung[1]) * mProBreitengrad;
        gpsDistanz[2] = gps[2];
    }
}
