package com.artur.softwareproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static com.artur.softwareproject.BluetoothConnectionList.EXTRA_FILES;

/**
 * Created by artur_000 on 01.05.2017.
 * A menu to display recorded sessions in a list.
 */

public class VRmenu extends AppCompatActivity
{

    private static final String TAG = VRmenu.class.getSimpleName();

    private final String path = "/ViSensor/Json";

    private final File pathName = new File(Environment.getExternalStorageDirectory().toString()
            + path);

    private VRmenuAdapter adapter;

    private Intent webServerIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vr_menu);

        if (getSupportActionBar() != null)
        {
            //implements the back button (android handles that by default)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArrayList<String> sessionFileList = new ArrayList<>();

        String[] fileNames = getIntent().getStringArrayExtra(EXTRA_FILES);

        if (fileNames == null)
        {
            fileNames = pathName.list();
        }

        Arrays.sort(fileNames,Collections.<String>reverseOrder());

        Collections.addAll(sessionFileList, fileNames);

        webServerIntent = new Intent(this, SimpleWebServer.class);

        startService(webServerIntent);

        ListView sessions = (ListView) findViewById(R.id.sessionList);

        adapter = new VRmenuAdapter(this, sessionFileList);

        sessions.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.vr_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        stopService(webServerIntent);
    }


    public VRmenuAdapter getAdapter()
    {
        return adapter;
    }
}

//EOF