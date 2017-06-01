package com.artur.softwareproject;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

/**
 * Created by artur_000 on 01.05.2017.
 */

public class VRmenu extends AppCompatActivity {
    private static final String TAG = VRmenu.class.getSimpleName();


    private String[] sessionFiles;
    private final String path = "/ViSensor/JSON";
    private final File pathName = new File(Environment.getExternalStorageDirectory() + path);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //implements the back button (android handles that by default)
        sessionFiles = pathName.list();
        for (String i : sessionFiles) {
            Log.d(TAG, i);
        }
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
