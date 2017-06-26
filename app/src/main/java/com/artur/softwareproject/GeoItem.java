package com.artur.softwareproject;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by root on 21.06.17.
 */

public class GeoItem implements ClusterItem
{
    private final LatLng mPosition;
    private final String filename;

    public GeoItem(double lat, double lng,String filename) {
        mPosition = new LatLng(lat, lng);
        this.filename = filename;

    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getFilename()
    {
        return filename;
    }
}
