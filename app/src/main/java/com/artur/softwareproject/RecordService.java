package com.artur.softwareproject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by Martin Kern on 27.05.2017.
 * This service saves sensor data in a JSON file.
 */

public class RecordService extends Service implements Runnable{

    private static final String TAG = RecordService.class.getSimpleName();

    //Variablen
    private double temperature;
    private double humidity;
    private double illuminance;
    private double[] pos = {0,0,0};
    private double[] gpsStartingPos = {0,0};
    private boolean bGps;
    private Runnable recService = this;
    final private IBinder recordBinder = new LocalBinder();
    final String path = "/ViSensor/Json";

    private boolean record = true;
    private boolean firstWrite = true;

    private File jsonFile;
    private String fileName;
    
    private ArrayList<double[]> positionList; //Positionsdaten, die vom ModelConstructor verwendet werden.

    Handler recordHandler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return recordBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Register BroadcastReceivers
        LocalBroadcastManager.getInstance(this).registerReceiver(temperatureReceive, new IntentFilter("temperatureFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(humidityReceive, new IntentFilter("humidityFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(opticalReceive, new IntentFilter("lightFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceive, new IntentFilter("gpsDistFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(baroReceive, new IntentFilter("hDiffFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsRawReceive, new IntentFilter("gpsFilter"));

        bGps = false;

        //Variablen initialisieren
        temperature = 0;
        humidity = 0;
        fileName = now();
        positionList = new ArrayList<>();

        //Directory, where thr JSON file is saved.
        File pathname = new File(Environment.getExternalStorageDirectory() + path);

        Log.d(TAG, Environment.getExternalStorageDirectory() + path);

        //If the directory does not exist then create it.
        if (!pathname.exists()) {
            if (!pathname.mkdir())
                Log.d(TAG, "Directory should have been created but wasn't.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        record = false;
    }

//BroadcastReceiver=================================================================================
//==================================================================================================

    private BroadcastReceiver temperatureReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            temperature = (double)intent.getExtras().get("ambientTemperature");
        }
    };

    private BroadcastReceiver gpsRawReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double gpsRaw[] = (double[])intent.getExtras().get("gpsRawData");

            if(gpsRaw == null) {
                gpsRaw = new double[2];
                gpsRaw[0] = 0;
                gpsRaw[1] = 0;
            }

            gpsStartingPos[0] = gpsRaw[0];
            gpsStartingPos[1] = gpsRaw[1];

            if (!bGps) {
                bGps = true;

                //Create JSON file
                jsonFile = new File(Environment.getExternalStorageDirectory() + path, fileName + ".json");

                //Write the beginning of the JSON file.
                try {
                    if(!jsonFile.createNewFile())
                        Log.d(TAG, "Failed to create a new json file.");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
                    writer.write(   "{\"coordinates\":{\n\"latitude\": " + gpsStartingPos[0] + ",\n" +
                            "\"longitude\": " + gpsStartingPos[1] + "\n},\n \"session\": [\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Starting the recording thread
                Thread recordThread = new Thread(recService);
                recordThread.start();
            }
        }
    };

    private BroadcastReceiver gpsReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] gps = (double[])intent.getExtras().get("gpsDistanz");

            if(gps == null) {
                gps = new double[2];
                gps[0] = 0;
                gps[1] = 0;
            }

            pos[0] = gps[0];
            pos[1] = gps[1];
        }
    };

    private BroadcastReceiver baroReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pos[2] = (double)intent.getExtras().get("hDiff");
        }
    };

    private BroadcastReceiver humidityReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            humidity = (double)intent.getExtras().get("humidity");
        }
    };

    private BroadcastReceiver opticalReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            illuminance = (double)intent.getExtras().get("light");
        }
    };
    

//BroadcastReceiver=End=============================================================================
//==================================================================================================

    //Creates a string that represents the current time.
    private String now() {
        GregorianCalendar now = new GregorianCalendar();
        String ret;

        String m = "" + (now.get(GregorianCalendar.MONTH)+1); //months start at 0
        String d = "" +  now.get(GregorianCalendar.DAY_OF_MONTH);
        String h = "" +  (now.get(GregorianCalendar.HOUR)  + 12 * now.get(GregorianCalendar.AM_PM));
        String min = "" +  now.get(GregorianCalendar.MINUTE);
        String s = "" +  now.get(GregorianCalendar.SECOND);

        if (m.length() == 1)
            m = "0" + m;
        if (d.length() == 1)
            d = "0" + d;
        if (h.length() == 1)
            h = "0" + h;
        if (min.length() == 1)
            min = "0" + min;
        if (s.length() == 1)
            s = "0" + s;

        ret = ""   + now.get(GregorianCalendar.YEAR)
                + "-" + m
                + "-" + d
                + "-" + h
                + "-" + min
                + "-" + s;

        return ret;
    }

    private String dataToJson() {

        return    "  {\n"
                + "    \"temperature\": " + Double.toString(temperature) + ",\n"
                + "    \"humidity\": " + Double.toString(humidity) + ",\n"
                + "    \"illuminance\": " + Double.toString(illuminance) + ",\n"
                + "    \"xPos\": " + Double.toString(pos[0]) + ",\n"
                + "    \"yPos\": " + Double.toString(pos[1]) + ",\n"
                + "    \"zPos\": " + Double.toString(pos[2]) + "\n"
                + "  }";
    }

    public class LocalBinder extends Binder {
        RecordService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RecordService.this;
        }
    }


    @Override
    public void run() {

        if (record) {

            double[] tempPos = new double[3];
            tempPos[0] = pos[0];
            tempPos[1] = pos[1];
            tempPos[2] = pos[2];

            positionList.add(tempPos);
            String string = dataToJson();

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
                if (firstWrite) {
                    writer.write(string);
                    firstWrite = false;
                } else {
                    writer.write(",\n" + string);
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {//write the end of json file
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
                writer.write("\n]}");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        recordHandler.postDelayed(this, 1000); //run every second
    }

    public int create3dModel() {
        Log.d(TAG, "About to call modelConstructor");
        double[][] posArray = new double[3][positionList.size()];
        posArray = positionList.toArray(posArray);
        return ModelConstructor.createModel(posArray, fileName, false);
    }
}

//EOF