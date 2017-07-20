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

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;


/**
 * Created by root on 02.07.17.
 * This class will combine multiple sessions, that were recorded close to each other, so that google
 * maps will only display one marker for all of them.
 */

class CustomClusterRenderer extends DefaultClusterRenderer<GeoItem>
{
    CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<GeoItem> clusterManager)
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
        try
        {
            String year = fn.substring(0, 4);
            String month = fn.substring(5, 7);
            String day = fn.substring(8, 10);
            String hour = fn.substring(11, 13);
            String minute = fn.substring(14, 16);
            String second = fn.substring(17, 19);

            return day + "." + month + "." + year + " " + hour + ":" + minute + ":" + second;
        }catch (Exception e)
        {
            return "No Datetime";
        }
    }
}

//EOF