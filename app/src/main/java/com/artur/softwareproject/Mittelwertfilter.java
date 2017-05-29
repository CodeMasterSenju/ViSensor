package com.artur.softwareproject;

/**
 * Created by Martin Kern on 18.05.2017.
 * Ein simpler FIR-Filter
 */

public class Mittelwertfilter {
    private double[] buffer;
    private int buffSize, buffPos;
    private boolean first = true;

    public Mittelwertfilter(int buffSize) {
        buffer = new double[buffSize];
        this.buffSize = buffSize;
        buffPos = 0;

        for (int i = 0; i < buffSize; i++) {
            buffer[i] = 0;
        }
    }

    public double output(double input) {

        if (first) {
            for (int i = 0; i < buffSize; i++) {
                buffer[i] = input;
            }
        } else
            buffer[buffPos] = input;

        buffPos++;
        if (buffPos >= buffSize) {
            buffPos = 0;
        }

        double sum = 0;
        for (int i = 0; i < buffSize; i++) {
            sum += buffer[i];
        }

        return sum/(double)buffSize;
    }
}
