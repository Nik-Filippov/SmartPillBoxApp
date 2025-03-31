package com.example.smartpillboxapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
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
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
    private String id;
    private boolean isEdit;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;
    private NotificationHelper notificationHelper;
//    private CalendarFragment calendarFragment;
    private static final String ARG_SELECTED_DATE = "selectedDate"; // Argument key
    private static final String ARG_SELECTED_NAME = "selectedPillName";
    private static final String ARG_SELECTED_COUNT = "selectedPillCount";
    private static final String ARG_SELECTED_RECURRENCE = "selectedRecurrence";
    private static final String ARG_SELECTED_CONTAINER = "selectedContainer";
    private static final String ARG_SELECTED_TIME = "selectedTime";
    private static final String ARG_IS_EDIT = "idEdit";
    private static final String ARG_ID = "id";
    private PillListViewHolder pillListViewHolder;
    private SharedViewModel sharedViewModel;
    private int[] numPills;


    public static AddReminderDialog newInstance(String date, String pillName, String pillCount, String recurrence, String container, String time, boolean isEdit, String id, PillListViewHolder pillListViewHolder) {
        AddReminderDialog fragment = new AddReminderDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_DATE, date);
        args.putString(ARG_SELECTED_NAME, pillName);
        args.putString(ARG_SELECTED_COUNT, pillCount);
        args.putString(ARG_SELECTED_RECURRENCE, recurrence);
        args.putString(ARG_SELECTED_CONTAINER, container);
        args.putString(ARG_SELECTED_TIME, time);
        args.putBoolean(ARG_IS_EDIT, isEdit);
        args.putString(ARG_ID, id);
        fragment.setArguments(args);
        fragment.pillListViewHolder = pillListViewHolder;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.add_reminder_dialog, container, false);

//        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
//
//        sharedViewModel.getNumPills().observe(this, pills -> {
//            numPills = pills;
//        });

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

        notificationHelper.createNotificationChannel(this.getContext());

        if (getArguments() != null) {
            selectedDate = getArguments().getString(ARG_SELECTED_DATE);
            selectedPillName = getArguments().getString(ARG_SELECTED_NAME);
            selectedPillCount = getArguments().getString(ARG_SELECTED_COUNT);
            selectedRecurrence = getArguments().getString(ARG_SELECTED_RECURRENCE);
            selectedContainer = getArguments().getString(ARG_SELECTED_CONTAINER);
            selectedTime = getArguments().getString(ARG_SELECTED_TIME);
            isEdit = getArguments().getBoolean(ARG_IS_EDIT);
            id = getArguments().getString(ARG_ID);
        }

        if (selectedDate != null) {
            dateButton.setText(selectedDate);
        } else {
            selectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            dateButton.setText(selectedDate);
        }

        if (selectedPillName != null) {
            pillNameEdit.setText(selectedPillName);
        }

        if (selectedTime != null) {
            timeButton.setText(selectedTime);
        }

        if (selectedPillCount != null) {
            setSpinnerValue(pillCountSpinner, selectedPillCount);
        }


        if (selectedRecurrence != null) {
            setSpinnerValue(recurrenceSpinner, selectedRecurrence);
        }


        if (selectedContainer != null) {
            setSpinnerValue(containerSpinner, selectedContainer);
        }


        if (selectedDate != null) {
            dateButton.setText(selectedDate);
        }

        dateButton.setOnClickListener(v -> {
            DatePickerFragment dateFragment = new DatePickerFragment();
            dateFragment.setDatePickerListener(date -> {
                dateButton.setText(date);
                selectedDate = date;
            });
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
//            Log.d("PillReminder", "Pill Name: " + selectedPillName);
//            Log.d("PillReminder", "Pill Count: " + selectedPillCount);
//            Log.d("PillReminder", "Recurrence: " + selectedRecurrence);
//            Log.d("PillReminder", "Container: " + selectedContainer);
//            Log.d("PillReminder", "Selected Date: " + selectedDate);
//            Log.d("SelectedTime", "Selected Time: " + selectedTime);


            if (isEdit){
                updateDatabase();
            }
            else {
                insertDatabase();
            }

//            readDatabase();

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

    private int calculateTwoWeekThreshold(String recurrence, String pillCount){
        SharedPreferences prefs = requireContext().getSharedPreferences("PillSettings", Context.MODE_PRIVATE);
        int notifyDays = prefs.getInt("notify_days", 14);

        Log.d("AddReminderDialog", "Notify Days = " + notifyDays);

        int pillAmount = Integer.parseInt(pillCount);
        if (recurrence.equals("Daily")){
            return notifyDays * pillAmount;
        }
        else if (recurrence.equals("Weekly")){
            return (notifyDays / 7) * pillAmount;
        }
        else if (recurrence.equals("Monthly")){
            return pillAmount;
        }
        return 0;
    }

    public void insertDatabase(){
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
            contentValues.put("reminderThreshold", calculateTwoWeekThreshold(selectedRecurrence, selectedPillCount));
            long result = sqLiteDatabase.insert("PillReminder", null, contentValues);
            if (result == -1) {
                Log.e("Database", "Insert failed");
            } else {
                Log.d("Database", "Insert successful");
                // Notify parent fragment or activity
                if (getParentFragment() instanceof ReminderInsertListener) {
                    ((ReminderInsertListener) getParentFragment()).onReminderInserted();
                }
            }
            // Schedule the alarm for the reminder
            setReminderAlarm(selectedPillName, storedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), selectedTime, selectedPillCount);

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

    public void updateDatabase(){
        String oldRecurrence = "";
        String oldPillName = selectedPillName;  // Store the new name as default
        if (id != null) {
            String query = "SELECT pill_name, recurrence FROM PillReminder WHERE id = ?";
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[] {id});
            if (cursor.moveToNext()) {  // Move directly since cursor should have only one row
                oldPillName = cursor.getString(0);  // Get the old name
                oldRecurrence = cursor.getString(1);  // Get the old recurrence
            }
            cursor.close(); // Always close the cursor

            // Delete ALL old reminders related to the old name, time, and recurrence
            pillListViewHolder.deleteAll(oldPillName, selectedTime, oldRecurrence);
        }

        // Insert the new reminder
        ContentValues contentValues = new ContentValues();
        LocalDate storedDate = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        LocalDate endDate = storedDate.plusYears(2);

        while (!storedDate.isAfter(endDate)) {
            contentValues.put("pill_name", selectedPillName); // NEW name
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
                // Notify parent fragment or activity
                if (getParentFragment() instanceof ReminderInsertListener) {
                    ((ReminderInsertListener) getParentFragment()).onReminderInserted();
                }
            }

            setReminderAlarm(selectedPillName, storedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), selectedTime, selectedPillCount);

            if (pillListViewHolder != null) {
                pillListViewHolder.checkForEmptyList();
            }

            // Handle recurrence
            if (selectedRecurrence.equals("Daily")) {
                storedDate = storedDate.plusDays(1);
            } else if (selectedRecurrence.equals("Weekly")) {
                storedDate = storedDate.plusWeeks(1);
            } else if (selectedRecurrence.equals("Monthly")) {
                storedDate = storedDate.plusMonths(1);
            } else {
                return;  // Stop if recurrence is "Does Not Repeat"
            }
        }
    }


    public void readDatabase(){
        String query = "SELECT * FROM PillReminder";
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
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

    public void setReminderAlarm(String pillName, String date, String time, String pillAmount) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(requireContext(), PillReminderReceiver.class);
        intent.putExtra("pill_name", pillName);
        intent.putExtra("pill_amount", pillAmount);

        // Convert date & time strings into Calendar object
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm a", Locale.ENGLISH);
        try {
            calendar.setTime(sdf.parse(date + " " + time));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Generate a unique request code for this reminder
        int requestCode = (pillName + date + time).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm for the exact time
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
