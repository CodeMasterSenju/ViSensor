package com.artur.softwareproject;

/**
 * Created by artur_000 on 01.05.2017.
 * Zeigt Sensordaten in Echtzeit an.
 */

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static android.os.SystemClock.sleep;


public class Main extends AppCompatActivity {

    private static final String TAG = Main.class.getSimpleName();

    public String [] datenTypen = {"Temperatur", "Luftfeuchtigkeit","Helligkeit","Position"};
    static public String [] datenEinheit = {"°C", "%", "lux", "m"};
    private ListView sensorDataList;
    private TextView recordClock;
    private SensorDataListAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private Intent serviceIntent;
    private Intent posServiceIntent;
    private Intent recordServiceIntent;
    private boolean record = false;
    private long currentTime;
    private int disconnect;
    private Thread disconnectThread;
    Handler updateHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordClock = (TextView) findViewById(R.id.recordClock);
        sensorDataList = (ListView) findViewById(R.id.dataList);
        adapter = new SensorDataListAdapter(this, datenTypen, datenEinheit);
        sensorDataList.setAdapter(adapter);
        disconnect = 0;

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(disconnectReceive, new IntentFilter("disconnectFilter"));

        serviceIntent = new Intent(this, BluetoothService.class);

        posServiceIntent = new Intent(this, PositionService.class);
        startService(posServiceIntent);

        recordServiceIntent = new Intent(this, RecordService.class);
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            ((BaseAdapter)adapter).notifyDataSetChanged();

            long millis = System.currentTimeMillis() - currentTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            recordClock.setText(String.format("Recorded time : " + "%02d:%02d", minutes, seconds));
            updateHandler.postDelayed(this, 500); //run every half a second
        }
    };

    @Override
    protected void onResume() {
        updateHandler.postDelayed(timerRunnable, 500);
        super.onResume();
    }

    @Override
    protected void onPause() {
        updateHandler.removeCallbacks(timerRunnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(serviceIntent);
        stopService(posServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public int getDisconnect()
    {
        return disconnect;
    }

    private BroadcastReceiver disconnectReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            disconnect = (int)intent.getExtras().get("disconnect");
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disconnect:
                stopService(serviceIntent);

                final ProgressDialog disconnectingDialog = new ProgressDialog(Main.this);
                disconnectingDialog.setMessage("Disconnecting...");
                disconnectingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                disconnectingDialog.show();

                final Handler disconnectHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        disconnectingDialog.dismiss();
                        disconnectThread.interrupt();
                        Intent bluetoothIntent = new Intent(Main.this, BluetoothConnectionList.class);
                        Main.this.startActivity(bluetoothIntent);
                        Main.this.finish();
                    }
                };

                disconnectThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int stop = 0;
                        while(stop == 0) {
                            stop = getDisconnect();
                            sleep(1500);
                        }
                        disconnectHandler.sendEmptyMessage(0);
                    }
                });
                disconnectThread.start();

                return true;

            case R.id.record_data:
                Intent resetIntent = new Intent();
                resetIntent.putExtra("reset", "");
                resetIntent.setAction("resetFilter");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resetIntent);

                if (!record) {
                    item.setIcon(R.drawable.ic_action_stop);
                    Toast.makeText(getApplicationContext(), "Start recording data", Toast.LENGTH_LONG).show();
                    recordClock.setVisibility(View.VISIBLE);

                    Animation a = AnimationUtils.loadAnimation(this, R.anim.textslide);
                    TextView tv = (TextView) findViewById(R.id.recordClock);
                    tv.startAnimation(a);

                    currentTime = System.currentTimeMillis();
                    record = true;
                    startService(recordServiceIntent);

                } else {
                    item.setIcon(R.drawable.ic_action_save);
                    Toast.makeText(getApplicationContext(), "Stop recording data", Toast.LENGTH_LONG).show();

                    Animation a = AnimationUtils.loadAnimation(this, R.anim.textupslide);
                    TextView tv = (TextView) findViewById(R.id.recordClock);
                    tv.startAnimation(a);

                    recordClock.setVisibility(View.GONE);
                    record = false;
                    stopService(recordServiceIntent);
                }

                return true;

            case R.id.vr_menu:
                Intent vrIntent = new Intent(Main.this, VRmenu.class);
                Main.this.startActivity(vrIntent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}