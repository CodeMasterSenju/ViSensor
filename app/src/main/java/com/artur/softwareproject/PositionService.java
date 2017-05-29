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
 * Dieser Service ermittelt die Aktuelle Position in karthesischen Koordinaten mit Hilfe von
 * GPS und Barometer.
 */


public class PositionService extends Service {
    private static final String TAG = PositionService.class.getSimpleName();
    //Variablen
    private double[] gps = {0,0,0}; // Längengrad, Breitengrad, Höhe
    private double[] ursprung = {0,0,0}; // Wird genutzt um sich die Ursprungsposition zu merken
    private double[] gpsDistanz = {0,0,0}; //Aktuelle Position in karthesischen Koordinaten in m
    private final double mProBreitengrad = 111133; //Abstand zwischen zwei Breitengraden in Metern.
    private boolean gpsInit = false;

    private double startDruck = 0; //Wird genutzt um sich den Druck zum Messbeginn zu merken
    private double druck = 0; //Aktueller Luftdruck
    private double hoehendifferenz = 0;
    private final double mProPascal = 0.11; //Pro Pascal ca. 11cm Höhenunterschied.
    private boolean baroInit = false;

    //Formalitäten
    private Intent gpsIntent, baroIntent;

    private BroadcastReceiver gpsReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            gps = (double[])intent.getExtras().get("gpsRawData");

            if (!gpsInit) {
                ursprung[0] = gps[0];
                ursprung[1] = gps[1];
                ursprung[2] = gps[2];
                gpsInit = true;
            }

            //Berechne karthesische Koordinaten.
            gpsDistanz[0] = (gps[0] - ursprung[0]) * mProBreitengrad * Math.cos(gps[1]*2*Math.PI / 360);
            gpsDistanz[1] = (gps[1] - ursprung[1]) * mProBreitengrad;
            gpsDistanz[2] = gps[2];

            Intent gpsDistIntent = new Intent();
            gpsDistIntent.putExtra("gpsDistanz", gpsDistanz);
            gpsDistIntent.setAction("gpsDistFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsDistIntent);
        }
    };

    private BroadcastReceiver baroReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            druck = (double)intent.getExtras().get("baroRawData");

            if (!baroInit) {
                startDruck = druck;
                baroInit = true;
            }

            hoehendifferenz = (startDruck - druck) * 100 * mProPascal;

            Intent hDiffIntent = new Intent();
            hDiffIntent.putExtra("hDiff", hoehendifferenz);
            hDiffIntent.setAction("hDiffFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hDiffIntent);
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "PositionService wurde erzeugt.");
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceive, new IntentFilter("gpsFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(baroReceive, new IntentFilter("baroFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(resetUrsprung, new IntentFilter("resetFilter"));
        gpsIntent = new Intent(this, GpsService.class);
        baroIntent = new Intent(this, BaroService.class);
        startService(gpsIntent);
        startService(baroIntent);
    }

    public void onDestroy() {
        super.onDestroy();
        stopService(gpsIntent);
        stopService(baroIntent);
    }


    //Setzt den Koordinatenursprung
    private BroadcastReceiver resetUrsprung = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ursprung[0] = gps[0];
            ursprung[1] = gps[1];
            ursprung[2] = gps[2];

            gpsDistanz[0] = 0;
            gpsDistanz[1] = 0;
            gpsDistanz[2] = 0;

            startDruck = druck;

            Intent gpsDistIntent = new Intent();
            gpsDistIntent.putExtra("gpsDistanz", gpsDistanz);
            gpsDistIntent.setAction("gpsDistFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsDistIntent);

            Intent hDiffIntent = new Intent();
            hDiffIntent.putExtra("hDiff", hoehendifferenz);
            hDiffIntent.setAction("hDiffFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hDiffIntent);
        }

    };
}

//EOF