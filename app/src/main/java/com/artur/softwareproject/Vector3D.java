package com.artur.softwareproject;

/**
 * Created by gabriel on 27.05.17.
 */

class Vector3D {
    public double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D( Vector2D v, double height)
    {
        this.x = v.dX;
        this.z = v.dY;
        this.y = height;
    }

    public Vector3D(Vector3D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public double getLength() {
        return (double) Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    }

    public void normalize() {
        double l = (double) (1 / Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z));
        this.x *= l;
        this.y *= l;
        this.z *= l;
    }

    public void add(Vector3D v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public void subtract(Vector3D v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    public void scale(double s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
    }

    public double dot(Vector3D v) {
        return this.x*v.x + this.y*v.y + this.z*v.z;
    }

    public Vector3D cross(Vector3D v) {
        return new Vector3D(this.y*v.z - this.z*v.y, this.z*v.x - this.x*v.z, this.x*v.y - this.y*v.x);
    }
}