package com.artur.softwareproject;

import android.support.annotation.NonNull;

/**
 * Created by gabriel on 27.05.17.
 */

class Vector3D implements Comparable<Vector3D>
{
    public double x, y, z, degree;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

//    public Vector3D( Vector2D v, double height)
//    {
//        this.x = v.dX;
//        this.z = v.dY;
//        this.y = height;
//    }

    public Vector3D(Vector3D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public double getLength() {
        return (double) Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    }

    public Vector3D normalize() {
        double l = (double) (1 / Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z));
        double x = this.x * l;
        double y = this.y * l;
        double z = this.z * l;
        return new Vector3D(x,y,z);
    }

    public Vector3D add(Vector3D v) {
        double x = this.x + v.x;
        double y = this.y + v.y;
        double z = this.z + v.z;
        return new Vector3D(x,y,z);
    }

    public Vector3D sub(Vector3D v) {
        double x = this.x - v.x;
        double y = this.y - v.y;
        double z = this.z - v.z;
        return new Vector3D(x,y,z);
    }

    public Vector3D scale(double s) {
        double x = this.x * s;
        double y = this.y * s;
        double z = this.z * s;
        return new Vector3D(x,y,z);
    }

    public double dot(Vector3D v) {
        return this.x*v.x + this.y*v.y + this.z*v.z;
    }

    public Vector3D cross(Vector3D v) {
        return new Vector3D(this.y*v.z - this.z*v.y, this.z*v.x - this.x*v.z, this.x*v.y - this.y*v.x);
    }

    public void setDegree(double degree)
    {
        this.degree = degree;
    }

    public double getDegree()
    {
        return degree;
    }

    @Override
    public int compareTo(@NonNull Vector3D o)
    {
        if(this.degree > o.getDegree())return -1;
        else if(this.degree < o.getDegree()) return 1;
        else return 0;
    }
}