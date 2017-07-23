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

/**
 * Created by Martin Kern on 28.05.2017.
 * A simple Kalman-Filter that is very good at filtering out noise.
 */

class SimpleKalmanFilter
{
    /*
   Filter parameters
   xk:   current guess
   xk_1: last guess
   pk:   current error covariance
   pk_1: last error covariance
   kk:   Kalman Gain
   stddeviation: standard deviation of the sensor
    */
    private double xk, xk_1, pk, pk_1, kk, measurementNoise, processNoise;
    private boolean first;

    /**
     * Initialize the Kalman Filter.
     *
     * @param stddev        standard deviation of the sensor
     * @param pNoise        process noise
     */

    SimpleKalmanFilter(double stddev, double pNoise)
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

    /**
     * Filter a signal.
     *
     * @param input Unfiltered signal coming from the sensor
     * @return The filtered signal.
     */

    double output(double input)
    {
        xk_1 = xk;
        pk_1 = pk + processNoise;

        kk = pk_1 / (pk_1 + measurementNoise);
        xk = xk + kk * (input - xk);
        pk = (1-kk) * pk_1;

        //The first input bypasses the filter.
        //This way other variables further down the line can be initialized with non zero values.
        if (first)
        {
            first = false;

            return input;
        }

        return xk_1;
    }
}

//EOF