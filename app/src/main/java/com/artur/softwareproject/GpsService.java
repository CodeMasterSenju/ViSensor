package com.artur.softwareproject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Martin Kern on 16.05.2017.
 * This service gets the data from the GPS module.
 */

public class GpsService extends Service implements LocationListener, Runnable {
    private static final String TAG = GpsService.class.getSimpleName();
    //variables
    private double[] gpsPosition = {0,0,0};
    private double[] xBuffer = {0,0,0,0};
    private double[] yBuffer = {0,0,0,0};
    private int index = 0;
    private boolean gpsStatus;
    private boolean gpsStatusUpdate = true;

    //formalities
    private LocationManager gpsManager;
    private Handler gpsStatusHandler = new Handler();
    private Thread gpsStatusThread;


    //onDestroy=========================================================================================
    @Override
    public void onDestroy() {
        gpsManager = null;
        gpsStatusUpdate = false;
        super.onDestroy();
    }
//onDestroy=end=====================================================================================

//LocationListener Overrides========================================================================

    public void onLocationChanged(Location location) {
        gpsPosition[0] = location.getLongitude();
        gpsPosition[1] = location.getLatitude();
        gpsPosition[2] = location.getAltitude();


        Intent gpsIntent = new Intent();
        gpsIntent.putExtra("gpsRawData", gpsPosition);
        gpsIntent.setAction("gpsFilter");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsIntent);

    }
    //
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {/*nothing*/}

    @Override
    public void onProviderEnabled(String provider) {/*nothing*/}

    @Override
    public void onProviderDisabled(String provider) {/*nothing*/}
//LocationListener Overrides=End====================================================================


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "GpsService was created.");

        gpsStatus = false;

        gpsManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkPermission("android.permission.ACCESS_FINE_LOCATION",1,0);
        gpsManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        gpsStatusThread = new Thread(this);
        gpsStatusThread.start();
    }

    @Override
    public void run(){
        if (!gpsStatusUpdate)
            return;

        xBuffer[index] = gpsPosition[0];
        yBuffer[index] = gpsPosition[1];
        index++;
        if (index > 3)
            index = 0;

        //If there was no update in the last 3 seconds we assume that the gps signal was lost.
        if (    xBuffer[0] == xBuffer[1] &&
                xBuffer[1] == xBuffer[2] &&
                xBuffer[2] == xBuffer[3] &&
                yBuffer[0] == yBuffer[1] &&
                yBuffer[1] == yBuffer[2] &&
                yBuffer[2] == yBuffer[3]) {
            if (gpsStatus) {
                gpsStatus = false;
                Intent gpsStatusIntent = new Intent();
                gpsStatusIntent.putExtra("gpsStatus", false);
                gpsStatusIntent.setAction("gpsStatusFilter");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsStatusIntent);
            }
        } else {
            if (!gpsStatus) {
                gpsStatus = true;
                Intent gpsStatusIntent = new Intent();
                gpsStatusIntent.putExtra("gpsStatus", true);
                gpsStatusIntent.setAction("gpsStatusFilter");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gpsStatusIntent);
            }
        }

        gpsStatusHandler.postDelayed(this, 1000);
    }


}

//EOF