package com.artur.softwareproject;

import android.support.annotation.NonNull;

/**
 * Created by gabriel on 27.05.17.
 */

public class Vector3D implements Comparable<Vector3D>
{
    public double x, y, z, comp;

    public Vector3D(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.comp = 0;
    }

    public Vector3D(Vector3D v)
    {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.comp = v.comp;
    }

    public double getLength()
    {
        return (double) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public Vector3D normalize()
    {
        double t = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        if(t==0)
            return new Vector3D(1,0,0);

        double l = (double) (1 / t);
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

    public Vector3D sub(Vector3D v)
    {
        double x = this.x - v.x;
        double y = this.y - v.y;
        double z = this.z - v.z;
        return new Vector3D(x, y, z);
    }

    public Vector3D scale(double s)
    {
        double x = this.x * s;
        double y = this.y * s;
        double z = this.z * s;
        return new Vector3D(x, y, z);
    }

    public double dot(Vector3D v)
    {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vector3D cross(Vector3D v)
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