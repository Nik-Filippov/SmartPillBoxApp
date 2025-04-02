package com.example.smartpillboxapp;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

public class PillListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView pillNameTextView, pillAmountTextView, pillTimeTextView, pillRecurrenceTextView;
    private final AdapterView.OnItemClickListener listener;
    private final Context context;
    private final String selectedDate;
    private final SQLiteDatabase sqLiteDatabase;
    private final DatabaseHelper dbHelper;
    private final PillListAdapter adapter;  // Reference to your adapter
    private final CalendarAdapter calendarAdapter;
    private final FragmentManager fragmentManager;
    private String pillName;
    private String pillCount;
    private String pillTime;
    private String pillRecurrence;


    public PillListViewHolder(@NonNull View itemView, AdapterView.OnItemClickListener listener, Context context, String selectedDate, DatabaseHelper dbHelper, PillListAdapter adapter, CalendarAdapter calendarAdapter, FragmentManager fragmentManager, String pillName, String pillCount, String pillTime, String pillRecurrence) {
        super(itemView);
        this.context = context; // Get context from itemView
        this.selectedDate = selectedDate;
        this.pillNameTextView = itemView.findViewById(R.id.pillNameTextView);
        this.pillAmountTextView = itemView.findViewById(R.id.pillAmountTextView);
        this.pillTimeTextView = itemView.findViewById(R.id.pillTimeTextView);
        this.pillRecurrenceTextView = itemView.findViewById(R.id.pillRecurrenceTextView);
        this.listener = listener;
        this.sqLiteDatabase = dbHelper.getReadableDatabase();
        this.dbHelper = dbHelper;
        this.adapter = adapter;
        this.calendarAdapter = calendarAdapter;
        this.fragmentManager = fragmentManager;
        this.pillName = pillName;
        this.pillCount = pillCount;
        this.pillTime = pillTime;
        this.pillRecurrence = pillRecurrence;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        pillName = pillNameTextView.getText().toString();
        pillTime = pillTimeTextView.getText().toString();
        pillRecurrence = pillRecurrenceTextView.getText().toString();
        String id;
        String pillContainer;

        // get id of pill
        String getIdQuery = "SELECT id, pill_amount, container FROM PillReminder WHERE pill_name = ? AND date = ? AND time = ? AND recurrence = ?";
        Cursor getIdCursor = sqLiteDatabase.rawQuery(getIdQuery, new String[]{pillName, selectedDate, pillTime, pillRecurrence});
        if (getIdCursor.getCount() == 0) {
            id = null;
            pillContainer = null;
            pillCount = null;
            Log.d("PillListViewHolder", "No pill ids returned");
        }
        else if (getIdCursor.getCount() > 1) {
            id = null;
            pillContainer = null;
            pillCount = null;
            Log.d("PillListViewHolder", "More than one pill returned with same id");
        }
        else {
            if (getIdCursor.moveToFirst()) {
                id = getIdCursor.getInt(0) + "";
                pillCount = getIdCursor.getInt(1) + "";
                pillContainer = getIdCursor.getInt(2) + "";
                Log.d("PillListViewHolder", "id: " + id + ", pillCount: " + pillCount + ", pillContainer: " + pillContainer);
            } else {
                id = null;
                pillContainer = null;
                pillCount = null;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(" " + pillName + " ")
                .setMessage(" " + selectedDate + " ")
                .setPositiveButton("Edit", ((dialog, which) -> {
                    AddReminderDialog addReminderDialog = new AddReminderDialog().newInstance(selectedDate, pillName, pillCount, pillRecurrence, pillContainer, pillTime, true, id, this);
                    addReminderDialog.show(fragmentManager, "AddReminderDialog");
                    if (adapter != null) {
                        adapter.removeItem(getAdapterPosition()); // Pass the position to the adapter to remove it
                    }
                    checkForEmptyList();
                }))
                .setNegativeButton("Delete", ((dialog, which) -> {

                    String query = "SELECT pill_name, time, recurrence, COUNT(*) AS count FROM PillReminder " +
                            "WHERE pill_name = ? AND time = ? AND recurrence = ? " +
                            "GROUP BY pill_name, time HAVING COUNT(*) > 1";
                    Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{pillName, pillTime, pillRecurrence});


                    if (cursor.getCount() == 0) { // If pill to delete is NOT recurring
                        Log.d("Database", "Nothing returned");
                        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(context);
                        confirmDelete.setTitle("Are you sure you want to delete?")
                                .setNegativeButton("Delete", (dialog2, which2) -> {
                                    deleteOnce(pillName, pillTime);
                                })
                                .setPositiveButton("Cancel", (dialog2, which2) -> {
                                    dialog2.dismiss();
                                });
                        AlertDialog confirmDeleteDialog = confirmDelete.create();
                        confirmDeleteDialog.show();
                    } else { // If pill to delete is recurring
                        // ONLY DO ThiS IF IT'S A RECURRING PILL
                        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);
                        deleteBuilder.setTitle("Delete Option")
                                .setMessage("Choose an option")
                                .setPositiveButton("Delete this pill", ((dialog1, which1) -> {
                                    AlertDialog.Builder confirmDelete = new AlertDialog.Builder(context);
                                    confirmDelete.setTitle("Are you sure you want to delete?")
                                            .setNegativeButton("Delete", ((dialog2, which2) -> {
                                                deleteOnce(pillName, pillTime);
                                            }))
                                            .setPositiveButton("Cancel", ((dialog2, which2) -> {
                                                dialog2.dismiss();
                                            }));
                                    AlertDialog confirmDeleteDialog = confirmDelete.create();
                                    confirmDeleteDialog.show();
                                }))
                                .setNegativeButton("Delete all recurring pills", ((dialog1, which1) -> {
                                    AlertDialog.Builder confirmDelete = new AlertDialog.Builder(context);
                                    confirmDelete.setTitle("Are you sure you want to delete?")
                                            .setNegativeButton("Delete", ((dialog2, which2) -> {
                                                deleteAll(pillName, pillTime, pillRecurrence);
                                            }))
                                            .setPositiveButton("Cancel", ((dialog2, which2) -> {
                                                dialog2.dismiss();
                                            }));
                                    AlertDialog confirmDeleteDialog = confirmDelete.create();
                                    confirmDeleteDialog.show();
                                }))
                                .setNeutralButton("Cancel", ((dialog1, which1) -> dialog1.dismiss()));

                        AlertDialog deleteDialog = deleteBuilder.create();
                        deleteDialog.show();
                    }
                }))
                .setNeutralButton("Cancel", ((dialog, which) -> {
                }));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteAll (String pillName, String pillTime, String pillRecurrence){
        // Query database for all associated dates before deletion
        Cursor cursor = sqLiteDatabase.query("PillReminder", new String[]{"date"},
                "pill_name = ? AND time = ? AND recurrence = ?",
                new String[]{pillName, pillTime, pillRecurrence},
                null, null, null);

        ArrayList<String> datesToDelete = new ArrayList<>();
        while (cursor.moveToNext()) {
            datesToDelete.add(cursor.getString(cursor.getColumnIndexOrThrow("date"))); // Collect all dates
        }
        cursor.close(); // Close cursor after retrieving data

        // Now delete the records
        long result = sqLiteDatabase.delete("PillReminder", "pill_name = ? AND time = ? AND recurrence = ?",
                new String[]{pillName, pillTime, pillRecurrence});

        if (result == -1) {
            Log.e("Database", "Deletion failed");
        } else {
            Log.d("Database", "Deletion successful");

            // Cancel alarms for all associated dates
            for (String date : datesToDelete) {
                cancelReminderAlarm(pillName, date, pillTime);
            }

            if (adapter != null) {
                Log.d("Database", "Notifying adapter to update list");
                adapter.removeItemsByPillDetails(pillName, pillTime, pillRecurrence);
            }

            checkForEmptyList();
        }
    }

    public void deleteOnce (String pillName, String pillTime){
        long result = sqLiteDatabase.delete("PillReminder", "pill_name = ? AND date = ? AND time = ?", new String[]{pillName, selectedDate, pillTime});
        if (result == -1) {
            Log.e("Database", "Deletion failed");
        } else {
            Log.d("Database", "Deletion successful: " + result);
            // Notify the adapter to remove the item
            cancelReminderAlarm(pillName, selectedDate, pillTime);
            if (adapter != null) {
                adapter.removeItem(getAdapterPosition()); // Pass the position to the adapter to remove it
            }
            checkForEmptyList();
        }
    }

    public void checkForEmptyList () {
        Log.d("PillListViewHolder", "In checkForEmptyList");

        // Query to load all pills for the selected date
        String query = "SELECT pill_name, pill_amount, time, recurrence FROM PillReminder WHERE date = ?";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{selectedDate});

        ArrayList<Pill> updatedPillList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            // No pills found → show empty message
            updatedPillList.add(new Pill("Pill List Empty", null, null, null));
        } else {
            // Load all pills into the list
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String amount = cursor.getInt(1) + "";
                String time = cursor.getString(2);
                String recurrence = cursor.getString(3);
                updatedPillList.add(new Pill(name, amount, time, recurrence));
            }
        }

        // Update the adapter with the full list
        adapter.updateList(updatedPillList);
        adapter.notifyDataSetChanged();

        // Update calendar dot
        if (calendarAdapter != null) {
            Log.d("PillListViewHolder", "In calendarAdapter");
            calendarAdapter.updateReminderDots();
        }

        cursor.close();
    }

    public void cancelReminderAlarm(String pillName, String date, String time) {
        Context context = itemView.getContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, PillReminderReceiver.class);
        intent.putExtra("pill_name", pillName);
        intent.putExtra("date", date);
        intent.putExtra("time", time);

        int requestCode = (pillName + date + time).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d("AlarmCancel", "Successfully canceled alarm.");
        } else {
            Log.d("AlarmCancel", "No alarm found to cancel.");
        }
    }
}