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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static android.os.SystemClock.sleep;


class VRmenuAdapter extends ArrayAdapter<String>
{
    private static final String TAG = VRmenuAdapter.class.getSimpleName();

    private ArrayList<String> fileNames;

    private AppCompatActivity context;

    private File jsonForDelete, objForDelete;

    private int currentPosition;

    VRmenuAdapter(AppCompatActivity context, ArrayList<String> fileNames)
    {
        super(context, R.layout.vr_menu_list_pattern, fileNames);

        this.fileNames = fileNames;
        this.context = context;

        currentPosition = 0;
    }


    private static class VrViewHolder
    {
        private TextView sessionName;
        private TextView sessionDate;
        private TextView sessionTime;
    }


    @Override @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent)
    {
        VrViewHolder mVrViewHolder;

        if (convertView == null)
        {
            mVrViewHolder = new VrViewHolder();

            LayoutInflater ListInflater = LayoutInflater.from(getContext());
            convertView = ListInflater.inflate(R.layout.vr_menu_list_pattern, parent, false);

            mVrViewHolder.sessionName = (TextView) convertView.findViewById(R.id.sessionName);
            mVrViewHolder.sessionDate = (TextView) convertView.findViewById(R.id.sessionDate);
            mVrViewHolder.sessionTime = (TextView) convertView.findViewById(R.id.sessionTime);

            convertView.setTag(mVrViewHolder);
        }
        else
        {
            mVrViewHolder = (VrViewHolder) convertView.getTag();
        }


        String sessionD = formatDate(fileNames.get(position));
        String sessionT = formatTime(fileNames.get(position));

        mVrViewHolder.sessionDate.setText(sessionD);
        mVrViewHolder.sessionTime.setText("   " + sessionT);
        mVrViewHolder.sessionName.setText(fileNames.get(position));

        convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

                String json = fileNames.get(position).split("\\.")[0];

                File objFile = new File(baseDirectory + "/ViSensor/OBJ/" +
                        fileNames.get(position).replace("json", "obj"));

                File jsonFile = new File(baseDirectory + "/ViSensor/JSON/" +
                        fileNames.get(position));

                String requestURL =
                        String.format("http://localhost:8080/index.html?file=%s?sensor=%s",
                                Uri.encode(json), Uri.encode("illuminance"));

                if (!jsonFile.exists())
                {
                    Log.d(TAG, "JSON file was not found.");

                    Toast.makeText(context.getApplicationContext(), "json file was not found.",
                            Toast.LENGTH_LONG).show();

                    remove(getItem(position));

                    notifyDataSetChanged();

                    objForDelete = new File(baseDirectory + "/ViSensor/OBJ/" +
                            fileNames.get(position).replace("json", "obj"));

                    //obj file without a json file is useless and can be deleted.
                    if (objForDelete.exists())
                    {
                        if (!objForDelete.delete())
                        {
                            Log.d(TAG, "Deleting obj file failed.");
                        }
                    }
                }
                else
                {
                    if (!objFile.exists())
                    {
                        Toast.makeText(context.getApplicationContext(), "obj file was not found.",
                                Toast.LENGTH_LONG).show();

                        Log.d(TAG, "OBJ file was not found");

                        sleep(1000);//Some time to read the message.
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
        convertView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

                objForDelete = new File(baseDirectory + "/ViSensor/OBJ/" +
                        fileNames.get(position).replace("json", "obj"));

                jsonForDelete = new File(baseDirectory + "/ViSensor/JSON/" +
                        fileNames.get(position));

                currentPosition = position;
                showDelDialog();

                return true;
            }
        });

        return convertView;
    }

    private String formatDate(String fn)
    {
        try
        {
            String year = fn.substring(0, 4);
            String month = fn.substring(5, 7);
            String day = fn.substring(8, 10);
            String hour = fn.substring(11, 13);
            String minute = fn.substring(14, 16);
            String second = fn.substring(17, 19);

            return day + "." + month + "." + year;
        }catch (Exception e)
        {
            return "No Datetime";
        }
    }

    private String formatTime(String fn)
    {
        try
        {
            String year = fn.substring(0, 4);
            String month = fn.substring(5, 7);
            String day = fn.substring(8, 10);
            String hour = fn.substring(11, 13);
            String minute = fn.substring(14, 16);
            String second = fn.substring(17, 19);

            return hour+":"+minute+":"+second;
        }catch (Exception e)
        {
            return "";
        }
    }

    private void showDelDialog()
    {
        DialogFragment delDialog = new FileDeleteDialog();

        notifyDataSetChanged();

        delDialog.show(context.getFragmentManager(), "FileDeleteDialog");
    }


    void onDialogPositiveClick()
    {
        Log.d(TAG, "VRmenuAdapter onPositiveClick: Check.");

        //delete json file
        if (jsonForDelete.exists())
        {
            if (!jsonForDelete.delete())
                Log.d(TAG, "Deleting json file failed.");
        }

        //delete obj file
        if (objForDelete.exists())
        {
            if (!objForDelete.delete())
            {
                Log.d(TAG, "Deleting obj file failed.");
            }
        }

        remove(getItem(currentPosition));

        notifyDataSetChanged();
    }
}

//EOF