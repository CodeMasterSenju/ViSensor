package com.artur.softwareproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class VRmenuAdapter extends ArrayAdapter{

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
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater ListInflater = LayoutInflater.from(getContext());
        View customView = ListInflater.inflate(R.layout.vr_menu_list_pattern, parent, false);

        sessionName = (TextView) customView.findViewById(R.id.sessionName);
        sessionCount = (TextView) customView.findViewById(R.id.sessionCount);

        sessionCount.setText("Session " + (position+1));
        sessionName.setText(fileNames[position]);

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return customView;
    }
}
