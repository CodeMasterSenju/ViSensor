package com.artur.softwareproject;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by Martin Kern on 01.06.2017.
 */

public class VRmenuAdapter extends ArrayAdapter{

    private String[] fileNames;
    private Context context;


    public VRmenuAdapter(Activity context, String[] fileNames){
        super(context, R.layout.bluetooth_list_pattern);
        this.fileNames = fileNames;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
