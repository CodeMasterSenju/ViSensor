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
    private final String path = "/ViSensor/Json";


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
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

        File pathname = new File(Environment.getExternalStorageDirectory() + path);

        Log.d(TAG, Environment.getExternalStorageDirectory() + path);
        if (!pathname.exists())
            pathname.mkdir();

        jsonFile = new File(Environment.getExternalStorageDirectory() + path, fileName);

        try {
            jsonFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
            writer.write("{\"session\": [\n");
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

        String m = "" + (now.get(GregorianCalendar.MONTH)+1); //Monate beginnen bei 0
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

        String ret =    "  {\n"
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