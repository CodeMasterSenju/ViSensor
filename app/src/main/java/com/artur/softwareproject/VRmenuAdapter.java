package com.artur.softwareproject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;

public class VRmenuAdapter extends ArrayAdapter{

    private static final String TAG = VRmenuAdapter.class.getSimpleName();

    private String[] fileNames;
    private Activity context;
    private TextView sessionName;
    private TextView sessionCount;

    public VRmenuAdapter(Activity context, String[] fileNames){
        super(context, R.layout.vr_menu_list_pattern, fileNames);
        this.fileNames = fileNames;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater ListInflater = LayoutInflater.from(getContext());
        View customView = ListInflater.inflate(R.layout.vr_menu_list_pattern, parent, false);

        sessionName = (TextView) customView.findViewById(R.id.sessionName);
        sessionCount = (TextView) customView.findViewById(R.id.sessionCount);

        sessionCount.setText("Session " + (position+1));
        sessionName.setText(fileNames[position]);

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
                String jsonFile = "JSON/" + fileNames[position];
                String objFile = "OBJ/" + fileNames[position].replace("json", "obj");
                Log.d(TAG, "JSON-File: " + jsonFile);
                Log.d(TAG, "OBJ-File: " + objFile);


                Uri webVRUri = Uri.parse("file://" + baseDirectory + "/ViSensor/index.html?sensor=temperature?file=" + jsonFile);
                Log.d(TAG,"URI: " + webVRUri.toString());

                Intent webVRIntent = new Intent(Intent.ACTION_VIEW);
                webVRIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                webVRIntent.setData(webVRUri);
                webVRIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                webVRIntent.setPackage("com.android.chrome");

                context.startActivity(webVRIntent);


            }
        });

        //Used to delete files.
        customView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
                String jsonFile = baseDirectory + "/JSON/" + fileNames[position];
                String objFile = baseDirectory + "/OBJ/" + fileNames[position].replace("json", "obj");
                return true;
            }
        });

        return customView;
    }
}

//EOF