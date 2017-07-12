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

import static java.lang.Math.pow;

/**
 * This class provides methods to convert the raw data from the bluetooth device into usable units.
 */

class ConvertData {

    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }

//    public static Integer shortSignedAtOffset(byte[] c, int offset) {
//        Integer lowerByte = (int) c[offset] & 0xFF;
//        Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
//        return (upperByte << 8) + lowerByte;
//    }

    double extractLightIntensity(byte[] v){
        int mantissa;
        int exponent;

        Integer sfloat= shortUnsignedAtOffset(v, 0);

        mantissa = sfloat & 0x0FFF;
        exponent = (sfloat >> 12) & 0xFF;

        double output;
        double magnitude = pow(2.0f, exponent);

        output = (mantissa * magnitude);

        return (output / 100.0f);
    }

    double extractHumidity(byte[] v){
        int a = shortUnsignedAtOffset(v,2);
        a = a - (a % 4);
        return (-6f) + 125f * (a / 65535f);
    }

    double extractAmbientTemperature(byte [] v) {
        int offset = 2;
        return shortUnsignedAtOffset(v, offset) / 128.0;
    }
}

//EOF