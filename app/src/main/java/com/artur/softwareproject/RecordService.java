package com.artur.softwareproject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;

/**
 * Created by Martin Kern on 27.05.2017.
 * Dieser Service speichert aufgezeichnete Daten in einer JSON-Datei
 */

public class RecordService extends Service implements Runnable{

    private static final String TAG = RecordService.class.getSimpleName();

    private double temperature;
    private double humidity;
    private double illuminance;
    private double xPos;
    private double yPos;
    private double zPos;

    Handler recordHandler = new Handler();

    private File jsonFile;
    private boolean record = true;
    private boolean firstWrite = true;


    private String fileName;
    private Thread recordThread;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Service created.");

        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(temperatureReceive, new IntentFilter("temperatureFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(humidityReceive, new IntentFilter("humidityFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(opticalReceive, new IntentFilter("lightFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceive, new IntentFilter("gpsDistFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(baroReceive, new IntentFilter("hDiffFilter"));

        temperature = 0;
        humidity = 0;
        xPos = 0;
        yPos = 0;
        zPos = 0;

        fileName = now() + ".json";

        jsonFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            jsonFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
            writer.write("{\"session\": [\n");
            Log.d(TAG, "Erste Zeile geschrieben.");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recordThread = new Thread(this);
        recordThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        record = false;

        Log.d(TAG, "Service wird zerstört.");

    }

    private BroadcastReceiver temperatureReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            temperature = (double)intent.getExtras().get("ambientTemperature");

        }
    };

    private BroadcastReceiver gpsReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] gps = (double[])intent.getExtras().get("gpsDistanz");
            xPos = gps[0];
            yPos = gps[1];
        }
    };

    private BroadcastReceiver baroReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            zPos = (double)intent.getExtras().get("hDiff");
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

    //Gibt die aktuelle Zeit als String aus
    private String now() {
        GregorianCalendar now = new GregorianCalendar();
        String ret;

        String m = "" + now.get(GregorianCalendar.MONTH);
        String d = "" +  now.get(GregorianCalendar.DAY_OF_MONTH);
        String h = "" +  now.get(GregorianCalendar.HOUR);
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

        String ret =    "  {\n"
                + "    \"time\": \"" + now() + "\",\n"
                + "    \"temperature\": " + Double.toString(temperature) + ",\n"
                + "    \"humidity\": " + Double.toString(humidity) + ",\n"
                + "    \"illuminance\": " + Double.toString(illuminance) + ",\n"
                + "    \"xPos\": " + Double.toString(xPos) + ",\n"
                + "    \"yPos\": " + Double.toString(yPos) + ",\n"
                + "    \"zPos\": " + Double.toString(zPos) + "\n"
                + "  }";

        return ret;
    }


    @Override
    public void run() {

        if (record) {
            Log.d(TAG, "run() funktioniert.");
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
        } else {
            Log.d(TAG, "run() sollte enden.");
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
}

//EOF