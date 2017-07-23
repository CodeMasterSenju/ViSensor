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

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by root on 21.06.17.
 * Used to create a marker in google maps.
 */

class GeoItem implements ClusterItem
{
    private final LatLng mPosition;
    private final String filename;

    GeoItem(double lat, double lng,String filename) {
        mPosition = new LatLng(lat, lng);
        this.filename = filename;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    String getFilename()
    {
        return filename;
    }
}

//EOF