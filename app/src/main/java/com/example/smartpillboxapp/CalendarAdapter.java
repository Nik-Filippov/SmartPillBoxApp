package com.example.smartpillboxapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final ArrayList<String> reminderDates;

    private final OnItemListener onItemListener;
    private final DatabaseHelper dbHelper;
    private int month;
    private int year;

    public CalendarAdapter(ArrayList<String> daysOfMonth, ArrayList<String> reminderDates, OnItemListener onItemListener, DatabaseHelper dbHelper, int month, int year)
    {
        this.daysOfMonth = daysOfMonth;
        this.reminderDates = reminderDates;
        this.onItemListener = onItemListener;
        this.dbHelper = dbHelper;
        this.month = month;
        this.year = year;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        String day = daysOfMonth.get(position); // Day of the month (e.g., "1", "2", "3")

        if (!day.isEmpty()) {
            // Convert to formatted date
            String formattedDate = getFormattedDate(year, month, Integer.parseInt(day));

            // Set text
            holder.dayOfMonth.setText(day);

            // Log the full date
//            Log.d("CalendarAdapter", "Date: " + formattedDate);

            if (hasRemindersForDate(formattedDate)) {
                holder.reminderDot.setVisibility(View.VISIBLE);
            } else {
                holder.reminderDot.setVisibility(View.GONE);
            }
        } else {
            holder.dayOfMonth.setText("");
            holder.reminderDot.setVisibility(View.GONE);
        }
    }

    private boolean hasRemindersForDate(String date) {
//        Log.d("CalendarAdapter", "Passes Date: " + date);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PillReminder WHERE date = '" + date + "'";
//        Log.d("CalendarAdapter", "QUERY: " + query);
        Cursor cursor = db.rawQuery(query, null);

        boolean hasReminder = false;
        if (cursor.getCount() != 0) {
            hasReminder = true;
        }
        cursor.close();
        db.close();
        return hasReminder;
    }

    private String getFormattedDate(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        return date.format(formatter);
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);

        void onReminderInserted();
    }
}