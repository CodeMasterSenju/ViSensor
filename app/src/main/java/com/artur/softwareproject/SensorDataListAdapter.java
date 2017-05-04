package com.artur.softwareproject;

/**
 * Created by artur_000 on 01.05.2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SensorDataListAdapter extends ArrayAdapter {

    private final Activity context;
    private final String[] datenTypen;
    private final String[] datenEinheit;
    private static float[] dataValues = {0,0,0,0};
    private TextView dataItemValue;
    private TextView dataItemName;
    private TextView dataItemType;

    public SensorDataListAdapter(Activity context, String[] datenTypen, String[] datenEinheit) {
        super(context, R.layout.sensordata_list_pattern, datenTypen);
        this.context = context;
        this.datenTypen = datenTypen;
        this.datenEinheit = datenEinheit;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater ListInflater = LayoutInflater.from(getContext());
        View customView = ListInflater.inflate(R.layout.sensordata_list_pattern, parent, false);

        dataItemName = (TextView) customView.findViewById(R.id.dataItemName);
        dataItemName.setText(datenTypen[position]);

        dataItemType = (TextView) customView.findViewById(R.id.dataItemType);
        dataItemType.setText(datenEinheit[position]);

        dataItemValue = (TextView) customView.findViewById(R.id.dataItemValue);
        //Here you enter the values recieved from SensorTag (instead of 2,3,4,5).
        dataValues[0] += 2;
        dataValues[1] += 3;
        dataValues[2] += 4;
        dataValues[3] += 5;
        dataItemValue.setText(Float.toString(dataValues[position]));

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What happens if you press on the list items at the main activity.
                Intent vrIntent = new Intent(v.getContext(), VRmenu.class);
                v.getContext().startActivity(vrIntent);
            }
        });

        return customView;
    }
}