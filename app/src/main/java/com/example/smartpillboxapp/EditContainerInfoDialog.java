package com.example.smartpillboxapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

public class EditContainerInfoDialog extends DialogFragment {

    private EditText etSubtitleEdit;
    private EditText etOnePillWeight;
    private TextView tvTotalWeight;
    private TextView tvNumberOfPills;

    private Button btnMeasureWeight;
    private Button btnSaveWeight;
    private Button btnSaveAndReturn;

    private double currentWeight; // Mocked Bluetooth weight
    private double onePillWeight;

    private FrameLayout viewPagerContainer;
    private FrameLayout fragmentContainer;
    private ViewPager2 viewPager;
    private SharedViewModel sharedViewModel;

    private int editedFieldIndex = 0;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's layout
        View view = inflater.inflate(R.layout.dialog_edit_container_info, container, false);

        // Initialize UI elements
        etSubtitleEdit = view.findViewById(R.id.etSubtitleEdit);
        etOnePillWeight = view.findViewById(R.id.etOnePillWeight);
        tvTotalWeight = view.findViewById(R.id.tvTotalWeight);
        tvNumberOfPills = view.findViewById(R.id.tvNumberOfPills);

        btnMeasureWeight = view.findViewById(R.id.btnMeasureWeight);
        btnSaveWeight = view.findViewById(R.id.btnSaveWeight);
        btnSaveAndReturn = view.findViewById(R.id.btnSave);

        // Load current subtitle from arguments
        if (getArguments() != null) {
            String currentSubtitle = "";
            if (getArguments().getString("current_subtitle_1") != null) {
                currentSubtitle = getArguments().getString("current_subtitle_1");
                editedFieldIndex = 1;
            } else if (getArguments().getString("current_subtitle_2") != null) {
                currentSubtitle = getArguments().getString("current_subtitle_2");
                editedFieldIndex = 2;
            } else if (getArguments().getString("current_subtitle_3") != null) {
                currentSubtitle = getArguments().getString("current_subtitle_3");
                editedFieldIndex = 3;
            }
            etSubtitleEdit.setText(currentSubtitle);
        }

        // Listen for Bluetooth data updates from Home Fragment
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe LiveData for updates
        sharedViewModel.getData().observe(getViewLifecycleOwner(), updatedData -> {
            tvTotalWeight.setText(updatedData);
        });

        // Button to measure weight
        btnMeasureWeight.setOnClickListener(v -> etOnePillWeight.setText(String.valueOf(currentWeight)));

        // Button to save one pill weight
        btnSaveWeight.setOnClickListener(v -> {
            onePillWeight = Double.parseDouble(etOnePillWeight.getText().toString());
            etOnePillWeight.setEnabled(false); // Make it uneditable
            updateWeightDisplay();
        });

        // Save and return to the previous screen
        btnSaveAndReturn.setOnClickListener(v -> {
            String subtitle = etSubtitleEdit.getText().toString();
            Bundle bundle = new Bundle();
            bundle.putString("new_subtitle", editedFieldIndex + subtitle);
            getParentFragmentManager().setFragmentResult("requestKey", bundle);
            dismiss();
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    private void updateWeightDisplay() {
        tvTotalWeight.setText(String.format("%.2f", currentWeight));
        if (onePillWeight > 0) {
            double numberOfPills = currentWeight / onePillWeight;
            tvNumberOfPills.setText(String.format("%.0f", numberOfPills));
        }
    }

    private void updateDataFields(String data) {
        // Split data received from Bluetooth
        String[] dataParts = data.split(";");

        // Example logic to parse and update fields
        if (dataParts.length >= 1) {
            currentWeight = Double.parseDouble(dataParts[0]);
            updateWeightDisplay();
        }
    }
}
