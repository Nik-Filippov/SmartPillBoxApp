package com.example.smartpillboxapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener, AddReminderDialog.ReminderInsertListener{
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    public void onReminderInserted() {
        refreshCalendar(); // Refresh the calendar when a reminder is inserted
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        try{
            dbHelper = new DatabaseHelper(this.getContext(), "PillReminderDatabase", null, 1);
            sqLiteDatabase = dbHelper.getWritableDatabase();
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PillReminder(pill_name TEXT, pill_amount INT, container INT, date TEXT, time TEXT, recurrence TEXT)");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        dbHelper = new DatabaseHelper(getContext(), "PillReminderDatabase", null, 1); // Initialize it here
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);
        selectedDate = LocalDate.now();
        Log.d("CalendarFragment", "Hello");
        setMonthView();

        // previous month button
        view.findViewById(R.id.previous_month).setOnClickListener(v -> previousMonthAction());

        // next month button
        view.findViewById(R.id.next_month).setOnClickListener(v -> nextMonthAction());
    }

    private void previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    private void nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }


    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));

        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        ArrayList<String> reminderDates = getReminderDatesForCalendar(selectedDate);

        int currentMonth = selectedDate.getMonthValue(); // 1-based (January = 1)
        int currentYear = selectedDate.getYear();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getContext(), "PillReminderDatabase", null, 1);
        }

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, reminderDates, this, dbHelper, currentMonth, currentYear);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireActivity().getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> getReminderDatesForCalendar(LocalDate date){
        ArrayList<String> reminderDates = new ArrayList<>();
        SQLiteDatabase db = new DatabaseHelper(getContext(), "PillReminderDatabase", null, 1).getReadableDatabase();
        YearMonth yearMonth = YearMonth.from(date);

        String monthString = date.format(DateTimeFormatter.ofPattern("yyyy-MM")); // Example: "2024-02"
        String query = "SELECT DISTINCT date FROM PillReminder WHERE date LIKE '" + monthString + "%'";

        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()){
            String fullDate = cursor.getString(0);
            LocalDate reminderDate = LocalDate.parse(fullDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            reminderDates.add(String.valueOf(reminderDate.getDayOfMonth()));
        }
        cursor.close();
        return reminderDates;
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        for (int i = 1; i <= 42; i++){
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek){
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")){
//            String message = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate);
//            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            AddReminderDialog addReminderFragment = new AddReminderDialog();
            Bundle args = new Bundle();
            addReminderFragment.show(getChildFragmentManager(), "AddReminderDialog");
        }
    }

    public void refreshCalendar() {
        setMonthView();
    }

}