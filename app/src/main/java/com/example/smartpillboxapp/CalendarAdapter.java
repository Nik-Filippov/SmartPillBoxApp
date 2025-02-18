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
            String formattedDate = getFormattedDate(year, month, Integer.parseInt(day));
            holder.dayOfMonth.setText(day);

            // Retrieve the pill name for the given date
            String pillName = getPillNameForDate(formattedDate);

            if (pillName != null && !pillName.isEmpty()) {
                holder.pillTaskTextView.setText(pillName);
                holder.pillTaskTextView.setVisibility(View.VISIBLE);  // Show the pill name
            } else {
                holder.pillTaskTextView.setVisibility(View.GONE);  // Hide pill name if none
            }
        } else {
            holder.dayOfMonth.setText("");
            holder.pillTaskTextView.setVisibility(View.GONE);
        }
    }

    private String getPillNameForDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<String> pillNames = new ArrayList<>();

        String query = "SELECT pill_name FROM PillReminder WHERE date = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        while (cursor.moveToNext()) {
            String pillName = cursor.getString(cursor.getColumnIndexOrThrow("pill_name"));
            pillNames.add(pillName);
        }

        cursor.close();
        db.close();

        // Check if there are more than 3 items
        if (pillNames.size() > 3) {
            // Limit to the first 3 items and add "..." at the end
            return String.join("\n", pillNames.subList(0, 3)) + "\n...";
        } else {
            // Return all pill names if there are 3 or fewer
            return String.join("\n", pillNames);
        }
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