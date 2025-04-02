package com.example.smartpillboxapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener, AddReminderDialog.ReminderInsertListener, AdapterView.OnItemClickListener{
    private TextView monthYearText, pillNameTextView, pillAmountTextView, pillTimeTextView, pillRecurrenceTextView;
    private RecyclerView calendarRecyclerView, pillListRecyclerView;
    private Button addPillsButton;
    private View dismissOverlay;
    private LocalDate selectedDate;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;
    private CalendarAdapter calendarAdapter;
    private String databaseDate;
    private SharedViewModel sharedViewModel;




    @Override
    public void onReminderInserted() {
        String currentSelectedDate = databaseDate;
        refreshCalendar(); // Refresh the calendar when a reminder is inserted
        showPillList(currentSelectedDate);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe the update trigger from HomeFragment
        sharedViewModel.getUpdateCalendar().observe(getViewLifecycleOwner(), update -> {
            if (update != null && update) {
                // Call method to refresh/update the calendar
                calendarAdapter.updateReminderDots();  // Your method to refresh the calendar
                sharedViewModel.resetCalendarUpdate(); // Reset the update trigger
            }
        });

        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

        try{
            dbHelper = new DatabaseHelper(this.getContext(), "PillReminderDatabase", null, 1);
            sqLiteDatabase = dbHelper.getWritableDatabase();
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PillReminder(id INTEGER PRIMARY KEY AUTOINCREMENT, pill_name TEXT, pill_amount INT, container INT, date TEXT, time TEXT, recurrence TEXT, reminderThreshold INT)");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(getContext(), "PillReminderDatabase", null, 1); // Initialize it here
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);
//        pillListTextView = view.findViewById(R.id.pillListTextView);
        pillNameTextView = view.findViewById(R.id.pillNameTextView);
        pillAmountTextView = view.findViewById(R.id.pillAmountTextView);
        pillTimeTextView = view.findViewById(R.id.pillTimeTextView);
        pillRecurrenceTextView = view.findViewById(R.id.pillRecurrenceTextView);

        pillListRecyclerView = view.findViewById(R.id.pillListRecyclerView);
        addPillsButton = view.findViewById(R.id.addPillsButton);
        dismissOverlay = view.findViewById(R.id.dismissOverlay);
        selectedDate = LocalDate.now();
        setMonthView();

        pillListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // previous month button
        view.findViewById(R.id.previous_month).setOnClickListener(v -> previousMonthAction());

        // next month button
        view.findViewById(R.id.next_month).setOnClickListener(v -> nextMonthAction());

        // close pill list when clicking outside of it
        dismissOverlay.setOnClickListener(v -> {
            if (pillListRecyclerView.getVisibility() == View.VISIBLE){
                pillListRecyclerView.setVisibility(View.GONE);
                addPillsButton.setVisibility(View.GONE);
                dismissOverlay.setVisibility(View.GONE);
//                pillListTextView.setVisibility(View.GONE);
                pillNameTextView.setVisibility(View.GONE);
                pillAmountTextView.setVisibility(View.GONE);
                pillTimeTextView.setVisibility(View.GONE);
                pillRecurrenceTextView.setVisibility(View.GONE);
            }
        });

        addPillsButton.setOnClickListener(v -> {
            String datePicked = databaseDate;
            AddReminderDialog addReminderFragment = new AddReminderDialog().newInstance(datePicked, null, null, null, null, null, false, null, null);
            addReminderFragment.show(getChildFragmentManager(), "AddReminderDialog");
        });



        // Prevent clicks inside the RecyclerView from propogating to the parent view
        pillListRecyclerView.setOnClickListener(v -> {});

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

        calendarAdapter = new CalendarAdapter(daysInMonth, reminderDates, this, dbHelper, currentMonth, currentYear);

        SharedPrefHelper.saveCalendarData(this.getContext(), daysInMonth, reminderDates, currentMonth, currentYear);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireActivity().getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> getReminderDatesForCalendar(LocalDate date){
        ArrayList<String> reminderDates = new ArrayList<>();
        SQLiteDatabase db = new DatabaseHelper(getContext(), "PillReminderDatabase", null, 1).getReadableDatabase();

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

    private String monthDayYearString(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        return date.format(formatter);
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")){

            LocalDate clickedDate = selectedDate.withDayOfMonth(Integer.parseInt(dayText));

            databaseDate = monthDayYearString(clickedDate);

            showPillList(databaseDate);

        }
    }

    public void showPillList(String date){
        ArrayList<Pill> pillItems = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT pill_name, pill_amount, time, recurrence FROM PillReminder WHERE date = '" + date + "'";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount() == 0){
            pillItems.add(new Pill("Pill List Empty", null, null, null));
        }
        else {
            while (cursor.moveToNext()) {
                String pill_name = cursor.getString(0);
                int pill_amount = cursor.getInt(1);
                String pill_time = cursor.getString(2);
                String pill_recurrence = cursor.getString(3);
                pillItems.add(new Pill(pill_name, "Amount: " + pill_amount, pill_time, pill_recurrence));
            }
        }
        cursor.close();

        // setting visibilities of pill list recycler view
        pillListRecyclerView.setVisibility(View.VISIBLE);
        addPillsButton.setVisibility(View.VISIBLE);
        pillNameTextView.setVisibility(View.VISIBLE);
        pillAmountTextView.setVisibility(View.VISIBLE);
        pillTimeTextView.setVisibility(View.VISIBLE);

        pillRecurrenceTextView.setVisibility(View.VISIBLE);
//        pillListTextView.setVisibility(View.VISIBLE);
        View dismissOverlay = getView().findViewById(R.id.dismissOverlay);
        dismissOverlay.setVisibility(View.VISIBLE);

        // setting adapter to list out pill names
        PillListAdapter adapter = new PillListAdapter(pillItems, (AdapterView.OnItemClickListener) this, this.getContext(), date, dbHelper, calendarAdapter);
        pillListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pillListRecyclerView.setAdapter(adapter);

        SharedPrefHelper.savePillItems(getContext(), pillItems);

    }

    public void refreshCalendar() {
        setMonthView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}

}