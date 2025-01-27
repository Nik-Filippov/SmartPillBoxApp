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
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvDateTime;
    private TextView tvData1Label, tvData2Label, tvData3Label;
    private TextView tvData1, tvData2, tvData3;
    private EditText etData1Subtitle, etData2Subtitle, etData3Subtitle;
    private Button btnEdit1, btnEdit2, btnEdit3;
    private SharedViewModel sharedViewModel1;

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

        updateDateTime();

        // "Edit" buttons' click listeners
        btnEdit1.setOnClickListener(v -> {
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("current_subtitle_1", etData1Subtitle.getText().toString());
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");
        });

        btnEdit2.setOnClickListener(v -> {
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("current_subtitle_2", etData2Subtitle.getText().toString());
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");
        });

        btnEdit3.setOnClickListener(v -> {
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("current_subtitle_3", etData3Subtitle.getText().toString());
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");
        });

        // Retrieving data from dialog triggered by "Edit" buttons
        getChildFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
            int editedFieldIndex = Integer.parseInt(result.getString("new_subtitle").substring(0,1));
            String subtitle = result.getString("new_subtitle").substring(1);
            if (editedFieldIndex == 1) {
                etData1Subtitle.setText(subtitle);
            } else if (editedFieldIndex == 2) {
                etData2Subtitle.setText(subtitle);
            } else if (editedFieldIndex == 3) {
                etData3Subtitle.setText(subtitle);
            }
        });

        // Send weight data to dialog
        sharedViewModel1 = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Listen for Bluetooth data updates
        BluetoothManager bluetoothManager = BluetoothManager.getInstance(getContext());
        bluetoothManager.setDataListener(new BluetoothManager.DataListener() {
            @Override
            public void onDataReceived(String data) {
                String[] dataParts = data.split(";");
                Handler mainHandler = new Handler(Looper.getMainLooper()); // Ensure handler is associated with main Looper
                mainHandler.post(() -> {
                    updateDataFields(dataParts); // Safely update UI on the main thread
                    sharedViewModel1.updateData(dataParts[0]);
                });
            }

            @Override
            public void onConnected(String message) {}

            @Override
            public void onError(String error) {}
        });

        return view;
    }

    private void updateDataFields(String[] dataParts) {
        if (dataParts.length == 3) {
            tvData1.setText(dataParts[0]);
            tvData2.setText(dataParts[1]);
            tvData3.setText(dataParts[2]);
        }
    }

    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd yyyy HH:mm", Locale.getDefault());
        Handler handler = new Handler(); // Use Handler associated with the main Looper
        handler.post(new Runnable() {
            @Override
            public void run() {
                String currentDateTime = dateFormat.format(new Date());
                requireActivity().runOnUiThread(() -> tvDateTime.setText(currentDateTime)); // Ensure UI update on the main thread
                handler.postDelayed(this, 1000); // Update every second
            }
        });
    }
}
