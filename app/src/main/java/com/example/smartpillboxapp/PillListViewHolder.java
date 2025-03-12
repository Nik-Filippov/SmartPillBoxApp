package com.example.smartpillboxapp;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PillListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView pillNameTextView, pillAmountTextView, pillTimeTextView, pillRecurrenceTextView;
    private final AdapterView.OnItemClickListener listener;
    private final Context context;
    private final String selectedDate;
    private final SQLiteDatabase sqLiteDatabase;
    private final DatabaseHelper dbHelper;
    private final PillListAdapter adapter;  // Reference to your adapter
    private final CalendarAdapter calendarAdapter;


    public PillListViewHolder(@NonNull View itemView, AdapterView.OnItemClickListener listener, Context context, String selectedDate, DatabaseHelper dbHelper, PillListAdapter adapter, CalendarAdapter calendarAdapter) {
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
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String pillName = pillNameTextView.getText().toString();
        String pillTime = pillTimeTextView.getText().toString();
        String pillRecurrence = pillRecurrenceTextView.getText().toString();
        Log.d("Database", "pillRecurrence: " + pillRecurrence);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(" " + pillName + " ")
                .setMessage(" " + selectedDate + " ")
                .setPositiveButton("Edit", ((dialog, which) -> {
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
                                .setPositiveButton("Cancel", (dialog2, which2) -> {dialog2.dismiss();});
                        AlertDialog confirmDeleteDialog = confirmDelete.create();
                        confirmDeleteDialog.show();
                    }
                    else { // If pill to delete is recurring
                        // ONLY DO ThiS IF IT'S A RECURRING PILL
                        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);
                        deleteBuilder.setTitle("Delete Option")
                                .setMessage("Choose an option")
                                .setPositiveButton("Delete this pill", ((dialog1, which1) -> {
                                    deleteOnce(pillName, pillTime);
                                }))
                                .setNegativeButton("Delete all recurring pills", ((dialog1, which1) -> {
                                    deleteAll(pillName, pillTime, pillRecurrence);
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

    public void deleteAll(String pillName, String pillTime, String pillRecurrence){
        long result = sqLiteDatabase.delete("PillReminder", "pill_name = ? AND time = ? AND recurrence = ?", new String[] {pillName, pillTime, pillRecurrence});
        if (result == -1){
            Log.e("Database", "Deletion failed");
        }
        else {
            Log.d("Database", "Deletion successful");
            if (adapter != null) {
                Log.d("Database", "Notifying adapter to update list");
                adapter.removeItemsByPillDetails(pillName, pillTime, pillRecurrence); // You can pass the necessary details to remove from the adapter
            }

            checkForEmptyList();
        }
    }

    public void deleteOnce(String pillName, String pillTime){
        long result = sqLiteDatabase.delete("PillReminder", "pill_name = ? AND date = ? AND time = ?", new String[] {pillName, selectedDate, pillTime});
        if (result == -1){
            Log.e("Database", "Deletion failed");
        }
        else {
            Log.d("Database", "Deletion successful: " + result);
            // Notify the adapter to remove the item
            if (adapter != null) {
                Log.d("Database", "In adapter");
                adapter.removeItem(getAdapterPosition()); // Pass the position to the adapter to remove it
            }
            checkForEmptyList();
        }
    }

    public void checkForEmptyList(){
        String query = "SELECT * FROM PillReminder WHERE date = '" + selectedDate + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount() == 0){
            // Show "Pill List Empty"
            ArrayList<Pill> emptyList = new ArrayList<>();
            emptyList.add(new Pill("Pill List Empty", null, null, null));
            // Update the existing RecyclerView
            adapter.updateList(emptyList);

            // Notify CalendarAdapter to remove the reminder dot
            if (calendarAdapter != null) {
                Log.d("In calendar adapter", "YAYAYA");
                calendarAdapter.updateReminderDots();
            }
        }
        cursor.close();
    }



}