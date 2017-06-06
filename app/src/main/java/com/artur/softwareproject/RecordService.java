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
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by Martin Kern on 27.05.2017.
 * Dieser Service speichert aufgezeichnete Daten in einer JSON-Datei
 */

public class RecordService extends Service implements Runnable{

    private static final String TAG = RecordService.class.getSimpleName();

    //Variablen
    private double temperature;
    private double humidity;
    private double illuminance;
    private double[] pos = {0,0,0};

    private boolean record = true;
    private boolean firstWrite = true;

    private File jsonFile;
    private final String path = "/ViSensor/Json";
    private String fileName;

    private ModelConstructor mConstr;
    private ArrayList<double[]> positionList; //Positionsdaten, die vom ModelConstructor verwendet werden.


    Handler recordHandler = new Handler();
    private Thread recordThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Registriere alle BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(temperatureReceive, new IntentFilter("temperatureFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(humidityReceive, new IntentFilter("humidityFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(opticalReceive, new IntentFilter("lightFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceive, new IntentFilter("gpsDistFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(baroReceive, new IntentFilter("hDiffFilter"));

        //Variablen initialisieren
        temperature = 0;
        humidity = 0;
        fileName = now();
        positionList = new ArrayList<>();
        mConstr = new ModelConstructor();

        //Verzeichnis, an dem JSON-Datei gespeichert wird.
        File pathname = new File(Environment.getExternalStorageDirectory() + path);

        Log.d(TAG, Environment.getExternalStorageDirectory() + path);

        //Existiert das Verzeichnis nicht, so wird es erzeugt.
        if (!pathname.exists())
            pathname.mkdir();

        //Erzeuge neue JSON-Datei
        jsonFile = new File(Environment.getExternalStorageDirectory() + path, fileName + ".json");

        //Schreibe den Anfang der JSON-Datei.
        try {
            jsonFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, true /*append*/));
            writer.write("{\"session\": [\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Starte den Thread zur Aufzeichnung
        recordThread = new Thread(this);
        recordThread.start();
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

    private BroadcastReceiver gpsReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] gps = (double[])intent.getExtras().get("gpsDistanz");
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
                + "    \"xPos\": " + Double.toString(pos[0]) + ",\n"
                + "    \"yPos\": " + Double.toString(pos[1]) + ",\n"
                + "    \"zPos\": " + Double.toString(pos[2]) + "\n"
                + "  }";

        return ret;
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
        } else {

            double[][] posArray = new double[3][positionList.size()];
            posArray = positionList.toArray(posArray);

            int created = mConstr.createModel(posArray, fileName, false);
            Log.d(TAG, "ModelCreator status: " + created);
            Intent mConstrIntent = new Intent();
            mConstrIntent.putExtra("modelConstructed", created);
            mConstrIntent.setAction("constructedFilter");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mConstrIntent);

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