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

import android.support.annotation.NonNull;

/**
 * Created by gabriel on 27.05.17.
 * This class provides methods to use 3d vectors
 */

class Vector3D implements Comparable<Vector3D>
{
    double x, y, z, comp;

    Vector3D(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.comp = 0;
    }

    Vector3D(Vector3D v)
    {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.comp = v.comp;
    }

    public double getLength()
    {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    Vector3D normalize()
    {
        double t = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        if(t==0)
            return new Vector3D(1,0,0);

        double l = 1.0d / t;
        double x = this.x * l;
        double y = this.y * l;
        double z = this.z * l;
        return new Vector3D(x, y, z);
    }

    public Vector3D add(Vector3D v)
    {
        double x = this.x + v.x;
        double y = this.y + v.y;
        double z = this.z + v.z;
        return new Vector3D(x, y, z);
    }

    Vector3D sub(Vector3D v)
    {
        double x = this.x - v.x;
        double y = this.y - v.y;
        double z = this.z - v.z;
        return new Vector3D(x, y, z);
    }

    Vector3D scale(double s)
    {
        double x = this.x * s;
        double y = this.y * s;
        double z = this.z * s;
        return new Vector3D(x, y, z);
    }

    double dot(Vector3D v)
    {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    Vector3D cross(Vector3D v)
    {
        return new Vector3D(this.y * v.z - this.z * v.y, this.z * v.x - this.x * v.z, this.x * v.y - this.y * v.x);
    }

    @Override
    public int compareTo(@NonNull Vector3D o)
    {
        if (this.comp > o.comp) return 1;
        else if (this.comp < o.comp) return -1;
        else return 0;
    }
}

//EOF