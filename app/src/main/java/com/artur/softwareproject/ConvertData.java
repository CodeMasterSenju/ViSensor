package com.artur.softwareproject;

import static java.lang.Math.pow;

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

    public double extractTargetTemperature(byte [] v, double ambient) {
        Integer twoByteValue = shortSignedAtOffset(v, 0);
        double Vobj2 = twoByteValue.doubleValue();
        Vobj2 *= 0.00000015625;

        double Tdie = ambient + 273.15;
        double S0 = 5.593E-14; // Calibration factor
        double a1 = 1.75E-3;
        double a2 = -1.678E-5;
        double b0 = -2.94E-5;
        double b1 = -5.7E-7;
        double b2 = 4.63E-9;
        double c2 = 13.4;
        double Tref = 298.15;
        double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * pow((Tdie - Tref), 2));
        double Vos = b0 + b1 * (Tdie - Tref) + b2 * pow((Tdie - Tref), 2);
        double fObj = (Vobj2 - Vos) + c2 * pow((Vobj2 - Vos), 2);
        double tObj = pow(pow(Tdie, 4) + (fObj / S), .25);

        return tObj - 273.15;
    }
}