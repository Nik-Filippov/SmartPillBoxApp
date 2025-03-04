package com.example.smartpillboxapp;

import static java.lang.Double.NaN;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.round;

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
    private TextView tvData1, tvData2, tvData3;
    private EditText etData1Subtitle, etData2Subtitle, etData3Subtitle;
    private Button btnEdit1, btnEdit2, btnEdit3;
    private SharedViewModel sharedViewModel;
    private int edited_container_number = 0;
    private double onePillWeightContainer1 = NaN;
    private double onePillWeightContainer2 = NaN;
    private double onePillWeightContainer3 = NaN;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvDateTime = view.findViewById(R.id.tvDateTime);

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
            edited_container_number = 1;
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("container_number", String.valueOf(edited_container_number));
            args.putString("current_subtitle", etData1Subtitle.getText().toString());
            args.putString("one_pill_weight", String.valueOf(onePillWeightContainer1));
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");
        });

        btnEdit2.setOnClickListener(v -> {
            edited_container_number = 2;
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("container_number", String.valueOf(edited_container_number));
            args.putString("current_subtitle", etData2Subtitle.getText().toString());
            args.putString("one_pill_weight", String.valueOf(onePillWeightContainer2));
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");
        });

        btnEdit3.setOnClickListener(v -> {
            edited_container_number = 3;
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("container_number", String.valueOf(edited_container_number));
            args.putString("current_subtitle", etData3Subtitle.getText().toString());
            args.putString("one_pill_weight", String.valueOf(onePillWeightContainer3));
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");
        });

        // Retrieving data from dialog triggered by "Edit" buttons
        getChildFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
            String one_pill_weight = result.getString("new_one_pill_weight");
            String subtitle = result.getString("subtitle");
            if (edited_container_number == 1) {
                etData1Subtitle.setText(subtitle);
                onePillWeightContainer1 = Double.parseDouble(one_pill_weight);
            } else if (edited_container_number == 2) {
                etData2Subtitle.setText(subtitle);
                onePillWeightContainer2 = Double.parseDouble(one_pill_weight);
            } else if (edited_container_number == 3) {
                etData3Subtitle.setText(subtitle);
                onePillWeightContainer3 = Double.parseDouble(one_pill_weight);
            }
        });

        // Send weight data to dialog
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Listen for Bluetooth data updates
        BluetoothManager bluetoothManager = BluetoothManager.getInstance(getContext());
        bluetoothManager.setDataListener(new BluetoothManager.DataListener() {
            @Override
            public void onDataReceived(String data) {
                String[] dataPartsStr = data.split(";");
                Double[] dataPartsDouble = new Double[dataPartsStr.length];
                Integer[] numPills = new Integer[dataPartsStr.length];
                Double[] onePillWeights = {onePillWeightContainer1, onePillWeightContainer2, onePillWeightContainer3};
                for(int i = 0; i < dataPartsStr.length; i++){
                    double weight = Double.parseDouble(dataPartsStr[i]);
                    // Normalize weight to get accurate grams
                    if(i == 0){
                        weight *= 20/38.90;
                    } else if (i == 1){
                        weight *= 20/38.70;
                    } else if (i == 2){
                        weight *= 20/37.05 - 0.45;
                    }
                    dataPartsStr[i] = String.format("%.2f", weight);
                    dataPartsDouble[i] = weight;
                    if(!Double.isNaN(onePillWeights[i])){
                        numPills[i] = Math.toIntExact(round(weight / onePillWeights[i]));
                    } else {
                        numPills[i] = MIN_VALUE;
                    }
                }
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    // The line below displays weight instead of pill counts
                    // updateDataFields(dataPartsDouble);
                    updateDataFields(numPills);
                    if(edited_container_number != 0) {
                        sharedViewModel.updateData(dataPartsStr[edited_container_number - 1]);
                    }
                });
            }

            @Override
            public void onConnected(String message) {}

            @Override
            public void onError(String error) {}
        });

        return view;
    }

    private void updateDataFields(Integer[] dataParts) {
        if (dataParts.length == 3) {
            if(dataParts[0] != MIN_VALUE){
                tvData1.setText(Integer.toString(dataParts[0]));
            } else {
                tvData1.setText("Set one pill weight");
            }
            if(dataParts[1] != MIN_VALUE){
                tvData2.setText(Integer.toString(dataParts[1]));
            } else {
                tvData2.setText("Set one pill weight");
            }
            if(dataParts[2] != MIN_VALUE){
                tvData3.setText(Integer.toString(dataParts[2]));
            } else {
                tvData3.setText("Set one pill weight");
            }
        }
    }

    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd yyyy HH:mm", Locale.getDefault());
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String currentDateTime = dateFormat.format(new Date());
                requireActivity().runOnUiThread(() -> tvDateTime.setText(currentDateTime));
                handler.postDelayed(this, 1000);
            }
        });
    }
}
