package com.example.smartpillboxapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvDateTime;
    private TextView tvData1Label, tvData2Label, tvData3Label;
    private TextView tvData1, tvData2, tvData3;
    private EditText etData1Subtitle, etData2Subtitle, etData3Subtitle;
    private Button btnEdit1, btnEdit2, btnEdit3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvDateTime = view.findViewById(R.id.tvDateTime);

        tvData1Label = view.findViewById(R.id.tvData1Label);
        tvData2Label = view.findViewById(R.id.tvData2Label);
        tvData3Label = view.findViewById(R.id.tvData3Label);

        tvData1 = view.findViewById(R.id.tvData1);
        tvData2 = view.findViewById(R.id.tvData2);
        tvData3 = view.findViewById(R.id.tvData3);

        etData1Subtitle = view.findViewById(R.id.etData1Subtitle);
        etData2Subtitle = view.findViewById(R.id.etData2Subtitle);
        etData3Subtitle = view.findViewById(R.id.etData3Subtitle);

        btnEdit1 = view.findViewById(R.id.btnEdit1);
        btnEdit2 = view.findViewById(R.id.btnEdit2);
        btnEdit3 = view.findViewById(R.id.btnEdit3);

        setupEditButton(btnEdit1, etData1Subtitle);
        setupEditButton(btnEdit2, etData2Subtitle);
        setupEditButton(btnEdit3, etData3Subtitle);

        updateDateTime();

        // Listen for Bluetooth data updates
        BluetoothManager bluetoothManager = BluetoothManager.getInstance(getContext());
        bluetoothManager.setDataListener(new BluetoothManager.DataListener() {
            @Override
            public void onDataReceived(String data) {
                requireActivity().runOnUiThread(() -> updateDataFields(data));
            }

            @Override
            public void onConnected(String message) {}

            @Override
            public void onError(String error) {}
        });

        return view;
    }

    private void setupEditButton(Button button, EditText editText) {
        button.setOnClickListener(v -> {
            boolean isEditable = editText.isEnabled();
            editText.setEnabled(!isEditable);
            button.setText(isEditable ? "Edit" : "Save");
        });
    }

    private void updateDataFields(String data) {
        // Split data by ';'
        String[] dataParts = data.split(";");

        // Ensure we have 3 parts
        if (dataParts.length == 3) {
            tvData1.setText(dataParts[0]);
            tvData2.setText(dataParts[1]);
            tvData3.setText(dataParts[2]);
        }
    }

    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd yyyy HH:mm", Locale.getDefault());
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentDateTime = dateFormat.format(new Date());
                tvDateTime.setText(currentDateTime);
                handler.postDelayed(this, 1000); // Update every second
            }
        }, 0);
    }
}
