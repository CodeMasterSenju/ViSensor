package com.artur.softwareproject;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;


/**
 * Created by artur_000 on 05.05.2017.
 */

public class BluetoothConnection {

    private static final String TAG = "BluetoothConnection";
    private static final String NAME = "ViSensor";
    private static final UUID myUUID = UUID.fromString("304a7cce-3120-11e7-93ae-92361f002671");

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private ConnectedThread mConnectedThread;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private final BluetoothAdapter adapter;
    Context mContext;

    public BluetoothConnection(Context context) {
        mContext = context;
        adapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        //The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            //Create an new listening server socket
            try {
                tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(NAME, myUUID);
                Log.d(TAG, "AcceptThread: Setting up Server using" + myUUID);
            } catch (IOException e) {
                Log.d(TAG, "AcceptThread: IOException: " + e.getMessage());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread running");

            BluetoothSocket socket = null;

            try {
                Log.d(TAG, "run: RFCOM server socket start.....");
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted conneciton.");
            } catch (IOException e) {
                Log.d(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            if(socket != null)
            {
                connected(socket, mmDevice);
            }
            Log.d(TAG, "END acceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Canceling of AcceptThread ServerSocket failed." + e.getMessage());
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + myUUID);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.d(TAG,"ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            //Always cancel discovery because it will slow down a connection
            adapter.cancelDiscovery();

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                //Close the socket
                Log.d(TAG, "ConnectThread: Could not connect to socket " + e.getMessage());
                try {
                    mmSocket.close();
                    Log.d(TAG, "ConnectThread: Socket closed.");
                } catch (IOException e1) {
                    Log.d(TAG, "ConnectThread: Could not close the connection to the socket " + e1.getMessage());
                }

                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + myUUID);
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            Log.d(TAG, "cancel: Closing client socket");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed." + e.getMessage());
            }
        }
    }

    /**
     * Start the bluetooth service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        //Cancel any thread attempting to make a connection.
        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then connectThread stars and attempts to make a connection with the other devices AcceptThread.
     */
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "starClient: Started.");

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth"
            , "Please wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progress dialog when connection is established
            mProgressDialog.dismiss();

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024]; //buffer store for the stream
            int bytes; //bytes returned from stream

            //Keep listening to the InputStream until an exception occurs
            while (true) {
                //Read from the input stream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.d(TAG, "write: Error reading from inputStream " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);

            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.d(TAG, "write: Error writing to outputStream " + e.getMessage());
            }
        }

        //Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connected(BluetoothSocket mmSocket,BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting");

        //Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write called.");
        mConnectedThread.write(out);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
    }

}