package com.example.smartpillboxapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private ArrayList<BluetoothDevice> devicesList = new ArrayList<>();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT = 2;
    private static final int REQUEST_BLUETOOTH_SCAN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnScan = findViewById(R.id.btnScan);
        ListView lvDevices = findViewById(R.id.lvDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvDevices.setAdapter(devicesAdapter);

        btnScan.setOnClickListener(view -> scanDevices());
        lvDevices.setOnItemClickListener((adapterView, view, i, l) -> connectToDevice(devicesList.get(i)));

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // For Android 12 and higher, request Bluetooth permissions dynamically
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            scanDevices();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            }, REQUEST_BLUETOOTH_CONNECT);
        }
    }

    // Start scanning for devices
    private void scanDevices() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        devicesList.clear();
        devicesAdapter.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            devicesAdapter.add(device.getName() + "\n" + device.getAddress());
            devicesList.add(device);
        }

        // Start Bluetooth discovery for other devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    // Handle the discovery results
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !devicesList.contains(device)) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    devicesAdapter.add(device.getName() + "\n" + device.getAddress());
                    devicesList.add(device);
                }
            }
        }
    };

    // Connect to the selected Bluetooth device
    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        new Thread(() -> {
            BluetoothSocket socket = null;
            InputStream inputStream = null;
            try {
                // Create an RFCOMM socket to the Bluetooth device
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                socket.connect();

                // Get the input stream for reading data
                inputStream = socket.getInputStream();

                // Start a loop to continuously read from the input stream
                byte[] buffer = new byte[1024];  // Buffer to store the incoming data
                int bytes; // Number of bytes read from the input stream
                while (true) {
                    try {
                        // Read from the input stream
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            // Convert the data to a string and process it
                            String data = new String(buffer, 0, bytes);

                            // Update the UI with the received data
                            runOnUiThread(() -> {
                                // Update the TextView with the new data
                                TextView tvData = findViewById(R.id.tvData);
                                tvData.setText("Data: " + data);
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break; // Exit the loop if there's an error reading data
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (socket != null) {
                        socket.close(); // Close the socket when done
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    // Handle pairing state changes
    private final BroadcastReceiver pairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                switch (bondState) {
                    case BluetoothDevice.BOND_BONDED:
                        Toast.makeText(MainActivity.this, "Device paired", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Toast.makeText(MainActivity.this, "Pairing in progress", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Toast.makeText(MainActivity.this, "Pairing failed", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(pairingReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        unregisterReceiver(pairingReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                scanDevices();
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}