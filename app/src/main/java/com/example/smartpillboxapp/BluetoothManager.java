package com.example.smartpillboxapp;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BluetoothManager {

    private static BluetoothManager instance;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice connectedDevice;
    private DataListener dataListener;

    private Context context;

    private BluetoothManager(Context context) {
        this.context = context.getApplicationContext(); // Use the application context
    }

    public static synchronized BluetoothManager getInstance(Context context) {
        if (instance == null) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null");
            }
            instance = new BluetoothManager(context);
        }
        return instance;
    }

    public void connectToDevice(BluetoothDevice device, DataListener listener) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        new Thread(() -> {
            try {
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                socket.connect();

                bluetoothSocket = socket;
                connectedDevice = device;

                if (listener != null) listener.onConnected("Connected to: " + device.getName());

                // Start listening for data
                listenForData();
            } catch (IOException e) {
                if (listener != null) listener.onError("Connection failed");
            }
        }).start();
    }

    private void listenForData() {
        try {
            InputStream inputStream = bluetoothSocket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                bytes = inputStream.read(buffer);
                if (bytes > 0 && dataListener != null) {
                    String data = new String(buffer, 0, bytes);
                    dataListener.onDataReceived(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    public interface DataListener {
        void onDataReceived(String data);
        void onConnected(String message);
        void onError(String error);
    }
}
