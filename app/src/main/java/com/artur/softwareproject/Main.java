/* Copyright 2017 Artur Baltabayev, Jean Büsche, Martin Kern, Gabriel Scheibler
 *
 * This file is part of ViSensor.
 *
 * ViSensor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViSensor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViSensor.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.artur.softwareproject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Locale;
import static android.os.SystemClock.sleep;

/**
 * Created by artur_000 on 01.05.2017.
 * This Activity shows all data in real time. It also offers a button to recording the data.
 */

public class Main extends AppCompatActivity {

    private static final String TAG = Main.class.getSimpleName();

    public String [] dataTypes = {"Temperature", "Humidity", "Illuminance", "Position"};//getResources().getStringArray(R.array.dataTyes);
    public String [] dataUnits = {"°C", "%", "lux", "m"};//getResources().getStringArray(R.array.dataUnits);

    private TextView recordClock;

    private SensorDataListAdapter adapter;

    private Handler updateHandler = new Handler();

    private Menu mainMenu;

    private Intent bluetoothServiceIntent;
    private Intent posServiceIntent;

    private boolean recording = false;

    private long currentTime;

    private int disconnect;

    private boolean modelConstructed;
    private boolean gpsStatus; //false: unavailable, true: available
    private boolean rBound;

    private ProgressDialog pd;
    private ProgressDialog disconDialog;

    private Thread disconnectThread;

    private RecordService rService;



//    private static final String SERVICE_INTENT_STATE = "serIntSt";
//    private static final String POSITION_SERVICE_INTENT_STATE = "posSerIntSt";
//    private static final String RECORDING_STATE = "rcrdSt";
//    private static final String CURRENT_TIME_STATE = "currTimeSt";
//    private static final String DISCONNECT_STATE = "disconSt";
//    private static final String MODEL_CONSTRUCTED_STATE = "modConstSt";
//    private static final String PD_STATE = "pdSt";
//    private static final String GPS_STATUS_STATE = "gpsStatSt";
//    private static final String SDISCONNECT_THREAD_STATE = "disconThrSt";
//    private static final String RECORD_sERVICE_STATE = "recServSt";
//    private static final String RBOUND_STATE = "rBoundSt";
//
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(RECORDING_STATE, recording);
//        savedInstanceState.putLong(CURRENT_TIME_STATE, currentTime);
//        savedInstanceState.putInt(DISCONNECT_STATE, disconnect);
//        savedInstanceState.putInt(MODEL_CONSTRUCTED_STATE, modelConstructed);
//        savedInstanceState.putBoolean(GPS_STATUS_STATE, gpsStatus);
//        savedInstanceState.putBoolean(RBOUND_STATE, rBound);
//        savedInstanceState.put
//
//
//
//        super.onSaveInstanceState(savedInstanceState);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate was called.");

        recordClock = (TextView) findViewById(R.id.recordClock);
        ListView sensorDataList = (ListView) findViewById(R.id.dataList);

        adapter = new SensorDataListAdapter(this, dataTypes, dataUnits);
        sensorDataList.setAdapter(adapter);

        disconnect = 0;
        modelConstructed = false;
        gpsStatus = false;

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(disconnectReceive, new IntentFilter("disconnectFilter"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(gpsStatusReceive, new IntentFilter("gpsStatusFilter"));

        bluetoothServiceIntent = new Intent(this, BluetoothService.class);
        posServiceIntent = new Intent(this, PositionService.class);

        startService(posServiceIntent);

    }


    Runnable timerRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            adapter.notifyDataSetChanged();

            long millis = System.currentTimeMillis() - currentTime;

            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;

            seconds = seconds % 60;

            recordClock.setText(String.format(Locale.GERMANY, "Recorded time : " + "%02d:%02d", minutes, seconds));

            updateHandler.postDelayed(this, 500); //run every half a second
        }
    };


    @Override
    protected void onResume()
    {
        updateHandler.postDelayed(timerRunnable, 500);

        super.onResume();
    }


    @Override
    protected void onPause()
    {
        updateHandler.removeCallbacks(timerRunnable);

        super.onPause();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        stopService(bluetoothServiceIntent);
        stopService(posServiceIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mainMenu = menu;

        return true;
    }


    public int getDisconnect()
    {
        return disconnect;
    }

    private BroadcastReceiver disconnectReceive = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            disconnect = (int)intent.getExtras().get("disconnect");
        }
    };


    //Stop recording, if gps signal is lost.
    private BroadcastReceiver gpsStatusReceive = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            gpsStatus = (boolean)intent.getExtras().get("gpsStatus");

            if (recording && !gpsStatus)
            {
                Log.d(TAG, "Calling stopRecording().");
                stopRecording();
            }
        }
    };


//==================================================================================================
//==================================================================================================

    private static class DisconnectHandlerClass extends Handler
    {
        private final WeakReference<Main> mTarget;

        DisconnectHandlerClass(Main context)
        {
            mTarget = new WeakReference<>(context);
        }

        @Override
        public void handleMessage (Message msg)
        {
            super.handleMessage(msg);

            Main target = mTarget.get();

            target.disconDialog.dismiss();
            target.disconnectThread.interrupt();

            Intent bluetoothIntent = new Intent(target, BluetoothConnectionList.class);

            target.startActivity(bluetoothIntent);

            target.finish();
        }
    }


//==================================================================================================
//==================================================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.disconnect:

                if (!recording)
                {
                    stopService(bluetoothServiceIntent);

                    final ProgressDialog disconnectingDialog = new ProgressDialog(Main.this);

                    disconnectingDialog.setMessage("Disconnecting...");
                    disconnectingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    disconnectingDialog.setCancelable(false);
                    disconnectingDialog.show();

                    disconDialog = disconnectingDialog;

                    final DisconnectHandlerClass disconnectHandler =
                            new DisconnectHandlerClass(this);

                    disconnectThread = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int stop = 0;

                            while (stop == 0)
                            {
                                stop = getDisconnect();
                                sleep(1500);
                            }

                            disconnectHandler.sendEmptyMessage(0);
                        }
                    });

                    disconnectThread.start();
                }

                return true;

            case R.id.record_data:

                Log.d(TAG, "Record button was Pressed. Gps status: " + gpsStatus);

                if (!recording && gpsStatus)
                {
                    Intent resetIntent = new Intent();

                    resetIntent.putExtra("reset", "");
                    resetIntent.setAction("resetFilter");

                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resetIntent);

                    item.setIcon(R.drawable.ic_action_stop);
                    item.setTitle(R.string.stop_recording);

                    Toast.makeText(getApplicationContext(), "Start recording data", Toast.LENGTH_LONG).show();

                    currentTime = System.currentTimeMillis();
                    recordClock.setVisibility(View.VISIBLE);

                    Animation a = AnimationUtils.loadAnimation(this, R.anim.textslide);
                    TextView tv = (TextView) findViewById(R.id.recordClock);

                    tv.startAnimation(a);

                    recording = true;

                    if (!rBound)
                    {
                        Intent intent = new Intent(this, RecordService.class);
                        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    }
                }
                else if (recording)
                {
                    stopRecording();
                }

                return true;

            case R.id.vr_menu:

                if (!recording)
                {
                    Intent vrIntent = new Intent(Main.this, VRmenuMap.class);
                    Main.this.startActivity(vrIntent);
                }

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }


//==================================================================================================
//==================================================================================================

    private ServiceConnection mConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service)
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RecordService.LocalBinder binder = (RecordService.LocalBinder) service;
            rService = binder.getService();
            rBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            rBound = false;
        }
    };


//==================================================================================================
//==================================================================================================

    private void stopRecording()
    {
        final Context context = this;

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute()
            {
                pd = new ProgressDialog(context);
                pd.setTitle("Processing...");
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setMessage("Please wait.");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                modelConstructed = rService.create3dModel();

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                if (pd!=null)
                {
                    pd.dismiss();
                }

                if (rBound)
                {
                    unbindService(mConnection);
                    rBound = false;
                }

                MenuItem item = mainMenu.findItem(R.id.record_data);

                item.setIcon(R.drawable.ic_action_save);
                item.setTitle(R.string.start_recording);

                Animation a = AnimationUtils.loadAnimation(context, R.anim.textupslide);
                TextView tv = (TextView) findViewById(R.id.recordClock);
                tv.startAnimation(a);

                recordClock.setVisibility(View.GONE);

                recording = false;

                if (modelConstructed)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Recording stopped.\n3D model created.",
                                   Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                                   "Recording stopped.\n3D model creation failed.",
                                   Toast.LENGTH_LONG).show();
                }
            }
        };

        task.execute((Void[])null);

    }


/*The following code was originally used to stop the record service. It worked just fine with the
  exception of a progress indicator. Deep going changes were needed to make the progress indicator work.
  This code is left as a reference.*/

//    private void stopRecording() {
//        final ProgressDialog modelConstructorDialog = new ProgressDialog(Main.this);
//        modelConstructorDialog.setMessage("Constructing 3D model...");
//        modelConstructorDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        modelConstructorDialog.show();
//
//        final Handler modelConstructorHandler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                modelConstructorDialog.dismiss();
//                modelConstructorThread.interrupt();
//
//                if (modelConstructed == 1)
//                    Toast.makeText(getApplicationContext(), "Recording stopped.\n3D model created.", Toast.LENGTH_LONG).show();
//                else if (modelConstructed == -1)
//                    Toast.makeText(getApplicationContext(), "Recording stopped.\n3D model creation failed.", Toast.LENGTH_LONG).show();
//                else
//                    Toast.makeText(getApplicationContext(), "A severe error occurred.", Toast.LENGTH_LONG).show();
//
//
//                modelConstructed = 0;
//            }
//        };
//
//        final Context thisActivity = this;
//
//        modelConstructorThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                Intent stopRecordIntent = new Intent(thisActivity, RecordService.class);
//                stopService(stopRecordIntent);
//                while(modelConstructed == 0) {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                modelConstructorHandler.sendEmptyMessage(0);
//
//            }
//        });
//        modelConstructorThread.start();
//
//        MenuItem item = mainMenu.findItem(R.id.record_data);
//
//        item.setIcon(R.drawable.ic_action_save);
//
//        Animation a = AnimationUtils.loadAnimation(this, R.anim.textupslide);
//        TextView tv = (TextView) findViewById(R.id.recordClock);
//        tv.startAnimation(a);
//
//        recordClock.setVisibility(View.GONE);
//        recording = false;
//    }
}

//EOF