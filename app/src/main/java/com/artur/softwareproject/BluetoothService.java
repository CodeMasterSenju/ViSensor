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

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.*;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.UUID;
import static java.util.UUID.fromString;

/**
 * Created by artur_000 on 14.05.2017.
 * This service handles the connection between smartphone and bluetooth device.
 */

public class BluetoothService extends Service{

    private static final String TAG = BluetoothService.class.getSimpleName();

    private final UUID UUID_NOT = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final UUID UUID_IRT_DATA = fromString("f000aa01-0451-4000-b000-000000000000");
    private final UUID UUID_IRT_CONF = fromString("f000aa02-0451-4000-b000-000000000000"); // 0: disable, 1: enable

    private final UUID UUID_HUM_DATA = fromString("f000aa21-0451-4000-b000-000000000000");
    private final UUID UUID_HUM_CONF = fromString("f000aa22-0451-4000-b000-000000000000"); // 0: disable, 1: enable

    private final UUID UUID_OPT_DATA = fromString("f000aa71-0451-4000-b000-000000000000");
    private final UUID UUID_OPT_CONF = fromString("f000aa72-0451-4000-b000-000000000000"); // 0: disable, 1: enable

    private BluetoothGatt mBluetoothGatt;

    private Intent serviceIntent;

    private int init;

    private BluetoothGattCharacteristic tempData;
    private BluetoothGattCharacteristic tempConf;

    private BluetoothGattCharacteristic humData;
    private BluetoothGattCharacteristic humConf;

    private BluetoothGattCharacteristic optData;
    private BluetoothGattCharacteristic optConf;

    public BluetoothService()
    {
        init = 0;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG,"Bluetoothservice was started.");

        this.serviceIntent = intent;

        if(intent != null)
        {
            BluetoothDevice bDevice;

            bDevice = (BluetoothDevice)intent.getExtras().get("device");

            if (bDevice != null)
            {
                mBluetoothGatt = bDevice.connectGatt(this, false, mGattCallback);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public void onDestroy() {

        Log.d(TAG,"Bluetoothservice was destroyed.");

        mBluetoothGatt.disconnect();

        stopService(serviceIntent);
    }


    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState)
                {

                    Intent mainIntent = new Intent();
                    switch (newState)
                    {
                        case BluetoothProfile.STATE_CONNECTED:

                            mainIntent.putExtra("connected", 1);
                            mainIntent.setAction("connectedFilter");

                            LocalBroadcastManager.getInstance(getApplicationContext()).
                                    sendBroadcast(mainIntent);

                            mBluetoothGatt.discoverServices();

                            break;

                        case BluetoothProfile.STATE_DISCONNECTED:

                            mainIntent.putExtra("disconnect", 1);
                            mainIntent.setAction("disconnectFilter");

                            LocalBroadcastManager.getInstance(getApplicationContext()).
                                    sendBroadcast(mainIntent);

                            break;

                        default:

                            break;
                    }
                }


                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    if(status == BluetoothGatt.GATT_SUCCESS)
                    {
                        for(BluetoothGattService service: gatt.getServices())
                        {
                            for(BluetoothGattCharacteristic characteristic : service.
                                    getCharacteristics())
                            {
                                if(characteristic.getUuid().equals(UUID_IRT_CONF))
                                {
                                    tempConf = characteristic;
                                }
                                else if(characteristic.getUuid().equals(UUID_IRT_DATA))
                                {
                                    tempData = characteristic;
                                }
                                else if(characteristic.getUuid().equals(UUID_HUM_CONF))
                                {
                                    humConf = characteristic;
                                }
                                else if(characteristic.getUuid().equals(UUID_HUM_DATA))
                                {
                                    humData = characteristic;
                                }
                                else if(characteristic.getUuid().equals(UUID_OPT_CONF))
                                {
                                    optConf = characteristic;
                                }
                                else if(characteristic.getUuid().equals(UUID_OPT_DATA))
                                {
                                    optData = characteristic;
                                }
                            }
                        }

                        tempConf.setValue(new byte[]{0x01});
                        mBluetoothGatt.writeCharacteristic(tempConf);
                    }
                }


                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  int status)
                {

                    if(characteristic.getUuid().equals(UUID_IRT_CONF))
                    {
                        humConf.setValue(new byte[]{0x01});
                        gatt.setCharacteristicNotification(humData, true);
                        mBluetoothGatt.writeCharacteristic(humConf);
                    }
                    else if(characteristic.getUuid().equals(UUID_HUM_CONF))
                    {
                        optConf.setValue(new byte[]{0x01});
                        gatt.setCharacteristicNotification(optData, true);
                        mBluetoothGatt.writeCharacteristic(optConf);
                    }
                    else if(characteristic.getUuid().equals(UUID_OPT_CONF))
                    {
                        gatt.setCharacteristicNotification(tempData, true);
                        mBluetoothGatt.readCharacteristic(tempData);
                    }
                }


                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic)
                {

                    if(characteristic.getUuid().equals(UUID_IRT_DATA))
                    {
                        ConvertData convert = new ConvertData();

                        byte[] tempValue = characteristic.getValue();
                        double tempUpdateValue = convert.extractAmbientTemperature(tempValue);

                        Intent mainIntent = new Intent();

                        mainIntent.putExtra("ambientTemperature", tempUpdateValue);
                        mainIntent.setAction("temperatureFilter");

                        LocalBroadcastManager.getInstance(getApplicationContext()).
                                sendBroadcast(mainIntent);
                    }
                    else if(characteristic.getUuid().equals(UUID_HUM_DATA))
                    {
                        ConvertData convert = new ConvertData();

                        byte[] humValue = characteristic.getValue();
                        double humUpdateValue = convert.extractHumidity(humValue);

                        Intent mainIntent = new Intent();

                        mainIntent.putExtra("humidity", humUpdateValue);
                        mainIntent.setAction("humidityFilter");

                        LocalBroadcastManager.getInstance(getApplicationContext()).
                                sendBroadcast(mainIntent);
                    }
                    else if(characteristic.getUuid().equals(UUID_OPT_DATA))
                    {
                        ConvertData convert = new ConvertData();

                        byte[] optValue = characteristic.getValue();
                        double optUpdateValue = convert.extractLightIntensity(optValue);

                        Intent mainIntent = new Intent();

                        mainIntent.putExtra("light", optUpdateValue);
                        mainIntent.setAction("lightFilter");

                        LocalBroadcastManager.getInstance(getApplicationContext()).
                                sendBroadcast(mainIntent);
                    }
                }


                @Override
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status)
                {

                    if(characteristic.getUuid().equals(UUID_IRT_DATA))
                    {
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_NOT);

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        mBluetoothGatt.writeDescriptor(descriptor);
                    }
                }


                @Override
                public void onDescriptorWrite(BluetoothGatt gatt,
                                              BluetoothGattDescriptor descriptor,
                                              int status)
                {
                    if(init == 0)
                    {
                        BluetoothGattDescriptor humDescriptor = humData.getDescriptor(UUID_NOT);

                        humDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        mBluetoothGatt.writeDescriptor(humDescriptor);

                        init = 1;
                    }
                    else if(init == 1)
                    {
                        BluetoothGattDescriptor optDescriptor = optData.getDescriptor(UUID_NOT);

                        optDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        mBluetoothGatt.writeDescriptor(optDescriptor);

                        init = 2;
                    }
                }
            };
}

//EOF