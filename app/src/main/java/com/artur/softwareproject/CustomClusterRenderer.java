package com.artur.softwareproject;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Arrays;

/**
 * Created by root on 02.07.17.
 */

public class CustomClusterRenderer extends DefaultClusterRenderer<GeoItem>
{
    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<GeoItem> clusterManager)
    {
        super(context, map, clusterManager);
        setMinClusterSize(1);
    }

    @Override
    protected void onBeforeClusterItemRendered(GeoItem item, MarkerOptions markerOptions)
    {
        markerOptions.title(formatDate(item.getFilename()));
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<GeoItem> cluster, MarkerOptions markerOptions)
    {
        /*Object[] items = (cluster.getItems().toArray());
        String[] titles = new String[items.length];
        for (int i = 0; i < items.length; i++)
        {
            titles[i] = ((GeoItem)(items[i])).getFilename();
        }
        Arrays.sort(titles);
        markerOptions.title(formatDate(titles[0])+"\n- "+formatDate(titles[titles.length-1]));*/
        markerOptions.title("list datasets");
        super.onBeforeClusterRendered(cluster, markerOptions);
    }

    private String formatDate(String fn)
    {
        String year = fn.substring(0,4);
        String month = fn.substring(5,7);
        String day = fn.substring(8,10);
        String hour = fn.substring(11,13);
        String minute = fn.substring(14,16);
        String second = fn.substring(17,19);

        return day+"."+month+"."+year+" "+hour+":"+minute+":"+second;
    }
}
