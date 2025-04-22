package com.example.smartpillboxapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Set;

public class SettingsFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private final ArrayList<BluetoothDevice> devicesList = new ArrayList<>();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT = 2;
    private static final int REQUEST_BLUETOOTH_SCAN = 3;
    TextView tvSelectedDevice;
    private RadioGroup radioGroupNotifications;
    private RadioButton rbOneWeek;
    private RadioButton rbTwoWeeks;
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private BluetoothDevice connectedDevice;

    private final Handler connectionStatusHandler = new Handler();
    private final Runnable connectionStatusRunnable = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            if (connectedDevice != null && bluetoothAdapter != null) {
                if (bluetoothAdapter.getBondedDevices().contains(connectedDevice)) {
                    tvSelectedDevice.setText("Connected to: " + connectedDevice.getName());
                } else {
                    tvSelectedDevice.setText("Disconnected");
                    connectedDevice = null;
                }
            }
            connectionStatusHandler.postDelayed(this, 5000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        radioGroupNotifications = view.findViewById(R.id.radioGroupNotifications);
        rbOneWeek = view.findViewById(R.id.rbOneWeek);
        rbTwoWeeks = view.findViewById(R.id.rbTwoWeeks);

        prefs = requireContext().getSharedPreferences("PillSettings", Context.MODE_PRIVATE);
        int notifyDays = prefs.getInt("notify_days", 14);

        if (notifyDays == 7){
            rbOneWeek.setChecked(true);
        } else {
            rbTwoWeeks.setChecked(true);
        }

        radioGroupNotifications.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedDays = (checkedId == R.id.rbOneWeek) ? 7 : 14;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("notify_days", selectedDays);
            editor.apply();
            updateThreshold();
        });

        Button btnScan = view.findViewById(R.id.btnScan);
        ListView lvDevices = view.findViewById(R.id.lvDevices);
        tvSelectedDevice = view.findViewById(R.id.tvSelectedDevice);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devicesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        lvDevices.setAdapter(devicesAdapter);

        btnScan.setOnClickListener(v -> scanDevices());
        lvDevices.setOnItemClickListener((adapterView, view1, i, l) -> connectToDevice(devicesList.get(i)));

        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return view;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            scanDevices();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            }, REQUEST_BLUETOOTH_CONNECT);
        }

        // Register receiver for disconnect events
        IntentFilter disconnectFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        requireContext().registerReceiver(disconnectReceiver, disconnectFilter);

        // Start polling for connection updates
        connectionStatusHandler.post(connectionStatusRunnable);

        return view;
    }

    private void scanDevices() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        requireContext().registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !devicesList.contains(device)) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    devicesAdapter.add(device.getName() + "\n" + device.getAddress());
                    devicesList.add(device);
                }
            }
        }
    };

    private final BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null && connectedDevice != null && device.getAddress().equals(connectedDevice.getAddress())) {
                connectedDevice = null;
                requireActivity().runOnUiThread(() -> {
                    tvSelectedDevice.setText("Disconnected");
                    Toast.makeText(context, "Device disconnected", Toast.LENGTH_SHORT).show();
                });
            }
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        BluetoothManager bluetoothManager = BluetoothManager.getInstance(getContext());
        bluetoothManager.connectToDevice(device, new BluetoothManager.DataListener() {
            @Override
            public void onDataReceived(String data) {}

            @SuppressLint("MissingPermission")
            @Override
            public void onConnected(String message) {
                connectedDevice = device;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    tvSelectedDevice.setText("Connected to: " + device.getName());
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireContext().unregisterReceiver(receiver);
            requireContext().unregisterReceiver(disconnectReceiver);
        } catch (IllegalArgumentException ignored) {}
        connectionStatusHandler.removeCallbacks(connectionStatusRunnable);
    }

    private void updateThreshold(){
        dbHelper = new DatabaseHelper(this.getContext(), "PillReminderDatabase", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int notifyDays = prefs.getInt("notify_days", 14);
        Cursor cursor = db.rawQuery("SELECT id, pill_amount, recurrence FROM PillReminder", null);
        int threshold = -1;
        if (cursor != null){
            while (cursor.moveToNext()){
                int id = cursor.getInt(0);
                int pillCount = cursor.getInt(1);
                String pillRecurrence = cursor.getString(2);
                if (pillRecurrence.equals("Daily")){
                    threshold = notifyDays * pillCount;
                } else if (pillRecurrence.equals("Weekly")){
                    threshold = (notifyDays / 7) * pillCount;
                } else if (pillRecurrence.equals("Monthly")){
                    threshold = pillCount;
                }
                Log.d("SettingsFragment", "pillRecurrence: " + pillRecurrence);
                Log.d("SettingsFragment", "notifyDays: " + notifyDays);
                Log.d("SettingsFragment", "threshold: " + threshold);

                ContentValues contentValues = new ContentValues();
                contentValues.put("reminderThreshold", threshold);
                db.update("PillReminder", contentValues, "id = ?", new String[]{String.valueOf(id)});
            }
            cursor.close();
        }
        dbHelper.close();
    }
}