package com.artur.softwareproject;

/**
 * Created by Martin Kern on 28.05.2017.
 * A simple Kalman-Filter that is very good at filtering out noise.
 */

public class SimpleKalmanFilter {
    /*
   Filterparameter
   xk:   aktuelle Schätzung
   xk_1: vergangene Schätzung
   pk:   aktuelle Fehlerkovarianz
   pk_1: vergangene Fehlerkovarianz
   kk:   Kalman Gain
   stddeviation: Standartabweichung des Sensors
    */
    private double xk, xk_1, pk, pk_1, kk, measurementNoise, processNoise;
    private boolean first;

    public SimpleKalmanFilter(double stddev, double pNoise)
    {
        xk = 0;
        xk_1 = 0;
        pk = 1;
        pk_1 = 1;
        kk = 1;
        measurementNoise = stddev;
        processNoise = pNoise;
        first = true;
    }

    public double output(double input)
    {
        xk_1 = xk;
        pk_1 = pk + processNoise;

        kk = pk_1 / (pk_1 + measurementNoise);
        xk = xk + kk * (input - xk);
        pk = (1-kk) * pk_1;

        if (first) {
            first = false;
            return input;
        }

        return xk_1;
    }
}
