package com.artur.softwareproject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Martin Kern on 16.05.2017.
 * Dieser Service Holt sich die Positionsdaten vom GPS-Modul
 */

public class GpsService extends Service implements LocationListener {
    private static final String TAG = GpsService.class.getSimpleName();
    //Variablen
    private double[] gpsPosition = {0,0,0};

    //Formalitäten
    private LocationManager gpsManager;


    //onDestroy=========================================================================================
    @Override
    public void onDestroy() {
        gpsManager = null;
        super.onDestroy();
    }
//onDestroy=Ende====================================================================================

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
    public void onStatusChanged(String provider, int status, Bundle extras) {/*nichts*/}

    @Override
    public void onProviderEnabled(String provider) {/*nichts*/}

    @Override
    public void onProviderDisabled(String provider) {/*nichts*/}
//LocationListener Overrides=Ende===================================================================


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "GpsService wurde erzeugt.");

        gpsManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkPermission("android.permission.ACCESS_FINE_LOCATION",1,0);
        gpsManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
}

//EOF