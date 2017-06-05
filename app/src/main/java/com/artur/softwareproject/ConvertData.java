package com.artur.softwareproject;

import static java.lang.Math.pow;

/**
 * This class provides methods to convert the raw data from the bluetooth device into usable units.
 */

public class ConvertData {

    public static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }

    public static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
        return (upperByte << 8) + lowerByte;
    }

    public double extractLightIntensity(byte[] v){
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

    public double extractHumidity(byte[] v){
        int a = shortUnsignedAtOffset(v,2);
        a = a - (a % 4);
        return (-6f) + 125f * (a / 65535f);
    }

    public double extractAmbientTemperature(byte [] v) {
        int offset = 2;
        return shortUnsignedAtOffset(v, offset) / 128.0;
    }
}

//EOF