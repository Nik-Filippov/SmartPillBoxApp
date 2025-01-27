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
    private double currentWeight;
    private double onePillWeight;
    private String current_subtitle;
    private SharedViewModel sharedViewModel;
    private int edited_container_number = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        return dialog;
    }

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
            edited_container_number = Integer.parseInt(getArguments().getString("container_number"));
            current_subtitle = getArguments().getString("current_subtitle");
            onePillWeight = Double.parseDouble(getArguments().getString("one_pill_weight"));
            etSubtitleEdit.setText(current_subtitle);
            if(onePillWeight != -1.0){
                etOnePillWeight.setText(Double.toString(onePillWeight));
            } else {
                etOnePillWeight.setText("");
            }
        }

        // Listen for Bluetooth data updates from Home Fragment
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe LiveData for updates
        sharedViewModel.getData().observe(getViewLifecycleOwner(), updatedData -> {
            tvTotalWeight.setText(updatedData);
            currentWeight = Double.parseDouble(updatedData);
        });

        // Button to measure weight
        btnMeasureWeight.setOnClickListener(v -> {
            etOnePillWeight.setText(String.valueOf(currentWeight));
            etOnePillWeight.setEnabled(true);
        });

        // Button to save one pill weight
        btnSaveWeight.setOnClickListener(v -> {
            onePillWeight = Double.parseDouble(etOnePillWeight.getText().toString());
            etOnePillWeight.setEnabled(false);
            updateWeightDisplay();
        });

        // Save and return to the previous screen
        btnSaveAndReturn.setOnClickListener(v -> {
            String subtitle = etSubtitleEdit.getText().toString();
            Bundle bundle = new Bundle();
            bundle.putString("subtitle", subtitle);
            bundle.putString("new_one_pill_weight", String.valueOf(onePillWeight));
            getParentFragmentManager().setFragmentResult("requestKey", bundle);
            dismiss();
        });

        return view;
    }

    // Helper methods
    private void updateWeightDisplay() {
        tvTotalWeight.setText(String.format("%.2f", currentWeight));
        if (onePillWeight > 0) {
            double numberOfPills = currentWeight / onePillWeight;
            tvNumberOfPills.setText(String.format("%.0f", numberOfPills));
        }
    }
}
