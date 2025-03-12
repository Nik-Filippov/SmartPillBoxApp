package com.example.smartpillboxapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddReminderDialog extends DialogFragment {

    private EditText pillNameEdit;
    private Spinner pillCountSpinner;
    private Spinner recurrenceSpinner;
    private Spinner containerSpinner;
    private Button dateButton;
    private Button timeButton;
    private Button saveButton;
    private String selectedDate;
    private String selectedTime;
    private String selectedPillName;
    private String selectedPillCount;
    private String selectedRecurrence;
    private String selectedContainer;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;
//    private CalendarFragment calendarFragment;
    private static final String ARG_SELECTED_DATE = "selectedDate"; // Argument key

    public static AddReminderDialog newInstance(String date) {
        AddReminderDialog fragment = new AddReminderDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.add_reminder_dialog, container, false);

        pillNameEdit = view.findViewById(R.id.pillNameEdit);

        pillCountSpinner = view.findViewById(R.id.pillCountSpinner);
        recurrenceSpinner = view.findViewById(R.id.recurrenceSpinner);
        containerSpinner = view.findViewById(R.id.containerSpinner);

        timeButton = view.findViewById(R.id.timeButton);
        dateButton = view.findViewById(R.id.dateButton);
        saveButton = view.findViewById(R.id.saveButton);

        setRecurrenceSpinner();
        setPillCountSpinner();
        setContainerSpinner();

        if (getArguments() != null) {
            selectedDate = getArguments().getString(ARG_SELECTED_DATE);
        }

        if (selectedDate != null) {
            dateButton.setText(selectedDate);
        } else {
            selectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            dateButton.setText(selectedDate);
        }


        if (selectedDate != null) {
            Log.d("AddReminderDialog", "selectedDate: " + selectedDate);
            dateButton.setText(selectedDate);
        }

        dateButton.setOnClickListener(v -> {
            DatePickerFragment dateFragment = new DatePickerFragment();
            dateFragment.setDatePickerListener(date -> {
                dateButton.setText(date);
                selectedDate = date;
            });
            Log.d(null, "test");
            dateFragment.show(getChildFragmentManager(), "DatePickerFragment");
        });

        timeButton.setOnClickListener(v -> {
            TimePickerFragment timeFragment = new TimePickerFragment();
            timeFragment.setTimePickerListener(time -> {
                timeButton.setText(time);
                selectedTime = time;
            });
            timeFragment.show(getChildFragmentManager(), "TimePickerFragment");
        });

        saveButton.setOnClickListener(v -> {
            // Add to database
            selectedPillName = pillNameEdit.getText().toString().trim();
            selectedPillCount = pillCountSpinner.getSelectedItem().toString();
            selectedRecurrence = recurrenceSpinner.getSelectedItem().toString();
            selectedContainer = containerSpinner.getSelectedItem().toString();

            if (selectedPillName.isEmpty() || selectedPillCount.equals("Select Pill Count") || selectedRecurrence.isEmpty()
                || selectedContainer.equals("Select Container") || selectedDate == null || selectedTime == null){
                Toast.makeText(requireContext(), "Please enter in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("PillReminder", "Pill Name: " + selectedPillName);
            Log.d("PillReminder", "Pill Count: " + selectedPillCount);
            Log.d("PillReminder", "Recurrence: " + selectedRecurrence);
            Log.d("PillReminder", "Container: " + selectedContainer);
            Log.d("PillReminder", "Selected Date: " + selectedDate);
            Log.d("SelectedTime", "Selected Time: " + selectedTime);
            insertDatabase(view);
            readDatabase(view);
            dismiss();
        });

        try{
            dbHelper = new DatabaseHelper(this.getContext(), "PillReminderDatabase", null, 1);
            sqLiteDatabase = dbHelper.getWritableDatabase();
//            sqLiteDatabase.execSQL("CREATE TABLE PillReminder(pill_name TEXT, pill_amount INT, container INT, date TEXT, time TEXT, recurrence TEXT)");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return view;

    }

    public void insertDatabase(View view){
        ContentValues contentValues = new ContentValues();
        LocalDate storedDate = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        LocalDate endDate = storedDate.plusYears(2); // Two years from stored date

        while(!storedDate.isAfter(endDate)) {
            contentValues.put("pill_name", selectedPillName);
            contentValues.put("pill_amount", Integer.parseInt(selectedPillCount));
            contentValues.put("container", Integer.parseInt(selectedContainer));
            contentValues.put("date", storedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            contentValues.put("time", selectedTime);
            contentValues.put("recurrence", selectedRecurrence);
            long result = sqLiteDatabase.insert("PillReminder", null, contentValues);
            if (result == -1) {
                Log.e("Database", "Insert failed");
            } else {
                Log.d("Database", "Insert successful");
                Log.d("AddReminderDialog", "Parent Fragment: " + getParentFragment().toString());
                // Notify parent fragment or activity
                if (getParentFragment() instanceof ReminderInsertListener) {
                    Log.d("AddReminderDialog", "In if getParentFragment()");
                    ((ReminderInsertListener) getParentFragment()).onReminderInserted();
                }
            }
            Log.d("AddReminderDiaglog", "storedDate: " + storedDate);
            if (selectedRecurrence.equals("Daily")){
                storedDate = storedDate.plusDays(1);
            }
            else if (selectedRecurrence.equals("Weekly")){
                storedDate = storedDate.plusWeeks(1);
            }
            else if (selectedRecurrence.equals("Monthly")) {
                storedDate = storedDate.plusMonths(1);
            }
            else {
                return;
            }
        }
    }

    public void readDatabase(View view){
//        String query = "SELECT * FROM PillReminder WHERE date = '" + selectedDate + "'";
        ArrayList<String> pillReminders = new ArrayList<>();
        String query = "SELECT * FROM PillReminder";
        Log.d("AddReminderDialog", "QUERY: " + query);
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
//            LocalDate today = LocalDate.now();
            while(cursor.moveToNext()) {
                String pillName = cursor.getString(1);
                int pillAmount = cursor.getInt(2);
                int container = cursor.getInt(3);
                String dateStr = cursor.getString(4);
                String time = cursor.getString(5);
                String recurrence = cursor.getString(6);
//                LocalDate storedDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MMMM d, yyyy"));
//                LocalDate twoYearsFromStored = storedDate.plusYears(2);
            }
            if (cursor.getCount() == 0) {
                Log.d("Database", "No reminders found for the selected date.");
            } else {
                while (cursor.moveToNext()) { // Iterate through all matching rows
                    Log.d("Database", "Pill Name: " + cursor.getString(0));
                    Log.d("Database", "Pill Amount: " + cursor.getInt(1));
                    Log.d("Database", "Container: " + cursor.getInt(2));
                    Log.d("Database", "Date: " + cursor.getString(3));
                    Log.d("Database", "Time: " + cursor.getString(4));
                    Log.d("Database", "Recurrence: " + cursor.getString(5));
                }
            }
            cursor.close();

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setContainerSpinner(){
        ArrayList<String> containerNums = new ArrayList<>();
        containerNums.add("Select Container");
        containerNums.add("1");
        containerNums.add("2");
        containerNums.add("3");
        ArrayAdapter<String> containerAdapter = new ArrayAdapter<>(requireContext(),androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item, containerNums);
        containerSpinner.setAdapter(containerAdapter);
    }

    public void setRecurrenceSpinner(){
        ArrayList<String> recurrences = new ArrayList<>();
        recurrences.add("Does not repeat");
        recurrences.add("Daily");
        recurrences.add("Weekly");
        recurrences.add("Monthly");
        ArrayAdapter<String> recurrenceAdapter = new ArrayAdapter<>(requireContext(), androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item, recurrences);
        recurrenceSpinner.setAdapter(recurrenceAdapter);
    }

    public void setPillCountSpinner(){
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

    public interface ReminderInsertListener {
        void onReminderInserted();
    }
}
