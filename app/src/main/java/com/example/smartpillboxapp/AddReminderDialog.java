package com.example.smartpillboxapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class AddReminderDialog extends DialogFragment {

    private EditText pillNameEdit;
    private Spinner pillCountSpinner;
    private Spinner recurrenceSpinner;
    private Button dateButton;
    private Button timeButton;
    private Button saveButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.add_reminder_dialog, container, false);

        pillNameEdit = view.findViewById(R.id.pillNameEdit);

        pillCountSpinner = view.findViewById(R.id.pillCountSpinner);
        recurrenceSpinner = view.findViewById(R.id.recurrenceSpinner);

        timeButton = view.findViewById(R.id.timeButton);
        dateButton = view.findViewById(R.id.dateButton);
        saveButton = view.findViewById(R.id.saveButton);

        setRecurrenceSpinner();
        setPillCountSpinner();

        dateButton.setOnClickListener(v -> {
            DatePickerFragment dateFragment = new DatePickerFragment();
            dateFragment.setDatePickerListener(date -> dateButton.setText(date));
            dateFragment.show(getChildFragmentManager(), "DatePickerFragment");
        });

        timeButton.setOnClickListener(v -> {
            TimePickerFragment timeFragment = new TimePickerFragment();
            timeFragment.setTimePickerListener(time -> timeButton.setText(time));
            timeFragment.show(getChildFragmentManager(), "TimePickerFragment");
        });

        return view;

    }

    public void setRecurrenceSpinner(){
        recurrenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ArrayList<String> recurrences = new ArrayList<>();
        recurrences.add("Does not repeat");
        recurrences.add("Daily");
        recurrences.add("Weekly");
        recurrences.add("Monthly");
        ArrayAdapter<String> recurrenceAdapter = new ArrayAdapter<>(requireContext(), androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item, recurrences);
        recurrenceSpinner.setAdapter(recurrenceAdapter);
    }

    public void setPillCountSpinner(){
        pillCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (position == 0) {
                    // Placeholder item selected, do nothing or show a message
                    return;
                }
                Toast.makeText(requireContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ArrayList<String> pillCounts = new ArrayList<>();
        pillCounts.add("Select Pill Count");
        for (int i = 1; i <= 20; i++){
            pillCounts.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),  // Use getContext() if inside a Dialog
                android.R.layout.simple_spinner_item,
                pillCounts
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pillCountSpinner.setAdapter(adapter);
    }
}
