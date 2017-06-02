package com.artur.softwareproject;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.File;

/**
 * Created by artur_000 on 01.05.2017.
 */

public class VRmenu extends AppCompatActivity {

    private String[] sessionFiles;
    private final String path = "/ViSensor/Json";
    private final File pathName = new File(Environment.getExternalStorageDirectory().toString() + path);

    private ListView sessions;
    private VRmenuAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //implements the back button (android handles that by default)
        sessionFiles = pathName.list();

        sessions = (ListView) findViewById(R.id.sessionList);
        adapter = new VRmenuAdapter(this, sessionFiles);
        sessions.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.vr_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
