/* Copyright 2017 Artur Baltabayev, Jean-Josef Büschel, Martin Kern, Gabriel Scheibler
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

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;


class VRmenuAdapter extends ArrayAdapter<String> {

    private static final String TAG = VRmenuAdapter.class.getSimpleName();

    private ArrayList<String> fileNames;
    private AppCompatActivity context;
    private File jsonForDelete, objForDelete;
    private int currentPosition;

    VRmenuAdapter(AppCompatActivity context, ArrayList<String> fileNames){
        super(context, R.layout.vr_menu_list_pattern, fileNames);
        this.fileNames = fileNames;
        this.context = context;
        currentPosition = 0;
    }

    private static class VrViewHolder {
        private TextView sessionName;
        private TextView sessionCount;
    }

    @Override @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        VrViewHolder mVrViewHolder;

        if (convertView == null) {
            mVrViewHolder = new VrViewHolder();

            LayoutInflater ListInflater = LayoutInflater.from(getContext());
            convertView = ListInflater.inflate(R.layout.vr_menu_list_pattern, parent, false);

            mVrViewHolder.sessionName = (TextView) convertView.findViewById(R.id.sessionName);
            mVrViewHolder.sessionCount = (TextView) convertView.findViewById(R.id.sessionCount);

            convertView.setTag(mVrViewHolder);
        } else {
            mVrViewHolder = (VrViewHolder) convertView.getTag();
        }


        String sessionNo = "Session " + (position+1);
        mVrViewHolder.sessionCount.setText(sessionNo);
        mVrViewHolder.sessionName.setText(fileNames.get(position));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

                String json = fileNames.get(position).split("\\.")[0];

                File objFile = new File(baseDirectory + "/ViSensor/OBJ/" + fileNames.get(position).replace("json", "obj"));
                File jsonFile = new File(baseDirectory + "/ViSensor/JSON/" + fileNames.get(position));

                String requestURL = String.format("http://localhost:8080/index.html?file=%s?sensor=%s", Uri.encode(json), Uri.encode("illuminance"));

                if (!jsonFile.exists()) {
                    Log.d(TAG, "JSON file was not found.");
                } else {
                    if (!objFile.exists()) {
                        Log.d(TAG, "OBJ file was not found");
                    }
                    Intent webVRIntent = new Intent(Intent.ACTION_VIEW);
                    webVRIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                    webVRIntent.setData(Uri.parse(requestURL));
                    webVRIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    webVRIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    webVRIntent.setPackage("com.android.chrome");//Use Google Chrome

                    context.startActivity(webVRIntent);
                }


            }
        });

        //Used to delete files.
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
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

        return convertView;
    }

    private void showDelDialog() {
        DialogFragment delDialog = new FileDeleteDialog();
        notifyDataSetChanged();
        delDialog.show(context.getFragmentManager(), "FileDeleteDialog");

    }

    void onDialogPositiveClick() {
        Log.d(TAG, "VRmenuAdapter onPositiveClick: Check.");

        if (jsonForDelete.exists()) {//delete json file
            if (!jsonForDelete.delete())
                Log.d(TAG, "Deleting json file failed.");
        }

        if (objForDelete.exists()) {//delete obj file
            if (!objForDelete.delete())
                Log.d(TAG, "Deleting obj file failed.");
        }

        remove(getItem(currentPosition));
        notifyDataSetChanged();
    }

}

//EOF