package com.artur.softwareproject;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class VRmenuAdapter extends ArrayAdapter {

    private static final String TAG = VRmenuAdapter.class.getSimpleName();

    private ArrayList<String> fileNames;
    //private String[] fileNames;
    private AppCompatActivity context;
    private TextView sessionName;
    private TextView sessionCount;
    private File jsonForDelete, objForDelete;
    private int currentPosition;


    public VRmenuAdapter(AppCompatActivity context, ArrayList<String> fileNames){
        super(context, R.layout.vr_menu_list_pattern, fileNames);
        this.fileNames = fileNames;
        this.context = context;
        currentPosition = 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater ListInflater = LayoutInflater.from(getContext());
        View customView = ListInflater.inflate(R.layout.vr_menu_list_pattern, parent, false);

        sessionName = (TextView) customView.findViewById(R.id.sessionName);
        sessionCount = (TextView) customView.findViewById(R.id.sessionCount);

        sessionCount.setText("Session " + (position+1));
        sessionName.setText(fileNames.get(position));

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Adjust the URI to the final format. Check for file existence.
                String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

                String json = "JSON/" + fileNames.get(position);
                String obj = "OBJ/" + fileNames.get(position).replace("json", "obj");

                File objFile = new File(baseDirectory + "/ViSensor/OBJ/" + fileNames.get(position).replace("json", "obj"));
                File jsonFile = new File(baseDirectory + "/ViSensor/JSON/" + fileNames.get(position));
                File html = new File(baseDirectory + "/ViSensor/halloWelt.html");

                Uri webVRUri = Uri.parse("content://com.android.provider/ViSensor/ViSensor/index.html?sensor=light?file=" + json);

                Intent webVRIntent = new Intent(Intent.ACTION_VIEW);
                webVRIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                webVRIntent.setData(webVRUri);
                webVRIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                webVRIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                webVRIntent.setPackage("com.android.chrome");//Use Google Chrome

                context.startActivity(webVRIntent);


            }
        });

        //Used to delete files.
        customView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
                objForDelete = new File(baseDirectory + "/ViSensor/OBJ/" + fileNames.get(position).replace("json", "obj"));
                jsonForDelete = new File(baseDirectory + "/ViSensor/JSON/" + fileNames.get(position));

                currentPosition = position;
                showDelDialog();

                return true;
            }
        });

        return customView;
    }

    public void showDelDialog() {
        DialogFragment delDialog = new FileDeleteDialog();
        notifyDataSetChanged();
        delDialog.show(context.getFragmentManager(), "FileDeleteDialog");

    }

    public void onDialogPositiveClick() {
        Log.d(TAG, "VRmenuAdapter onPositiveClick: Check.");

        if (jsonForDelete.exists())//delete json file
            jsonForDelete.delete();

        if (objForDelete.exists())//delete obj file
            objForDelete.delete();

        remove(getItem(currentPosition));
        notifyDataSetChanged();
    }

}

//EOF