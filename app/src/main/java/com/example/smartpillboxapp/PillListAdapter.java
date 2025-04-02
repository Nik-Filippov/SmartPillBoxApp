package com.example.smartpillboxapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PillListAdapter extends RecyclerView.Adapter<PillListViewHolder> {
    private ArrayList<Pill> pillList;
    private Context context;
    private AdapterView.OnItemClickListener listener;
    private String selectedDate;
    private String pillName;
    private String pillCount;
    private String pillTime;
    private String pillRecurrence;
    private DatabaseHelper dbHelper;
    private CalendarAdapter calendarAdapter;

    public PillListAdapter(ArrayList<Pill> pillNames, AdapterView.OnItemClickListener listener, Context context, String selectedDate, DatabaseHelper dbHelper, CalendarAdapter calendarAdapter) {
        this.pillList = pillNames;
        this.listener = listener;
        this.context = context;
        this.selectedDate = selectedDate;
        this.dbHelper = dbHelper;
        this.calendarAdapter = calendarAdapter;
    }

    @NonNull
    @Override
    public PillListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pill_list_item, parent, false);
        return new PillListViewHolder(view, listener, context, selectedDate, dbHelper, this, calendarAdapter, ((FragmentActivity) parent.getContext()).getSupportFragmentManager(), pillName, pillCount, pillTime, pillRecurrence);
    }

    @Override
    public void onBindViewHolder(@NonNull PillListViewHolder holder, int position) {
        Pill pill = pillList.get(position);

        pillName = pill.getPillName();
        pillCount = pill.getPillAmount();
        pillTime = pill.getPillTime();
        pillRecurrence = pill.getPillRecurrence();
//        holder.pillListTextView.setText(pillName);
        holder.pillNameTextView.setText(pill.getPillName());
        holder.pillAmountTextView.setText(pill.getPillAmount());
        holder.pillTimeTextView.setText(pill.getPillTime());
        holder.pillRecurrenceTextView.setText(pill.getPillRecurrence());

        String pill_recurrence = pill.getPillRecurrence();

        if (pill.getPillName().equals("Pill List Empty")){
            holder.itemView.setClickable(false);
            holder.itemView.setAlpha(0.5f);
        }

        if (pill_recurrence != null && (pill_recurrence.equals("Daily") || pill_recurrence.equals("Weekly") || pill_recurrence.equals("Monthly"))){
            holder.pillRecurrenceTextView.setVisibility(View.VISIBLE);
        }
        else {
            holder.pillRecurrenceTextView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return pillList.size();
    }

    // Method to remove an item from the list
    public void removeItem(int position) {
        if (position >= 0 && position < pillList.size()) {
            pillList.remove(position); // Remove the item from the list
            notifyItemRemoved(position); // Notify the adapter that an item has been removed
            notifyItemRangeChanged(position, pillList.size());
        }
    }

    public void updateList(ArrayList<Pill> newList) {
        this.pillList.clear();
        this.pillList.addAll(newList);
        notifyDataSetChanged();
    }

    public void removeItemsByPillDetails(String pillName, String pillTime, String pillRecurrence) {
        for (int i = pillList.size() - 1; i >= 0; i--) {
            Pill pill = pillList.get(i);
            // Check if the pill matches the specified details
            if (pill.getPillName().equals(pillName) &&
                    pill.getPillTime().equals(pillTime) &&
                    pill.getPillRecurrence().equals(pillRecurrence)) {
                pillList.remove(i);  // Remove the item from the list
                notifyItemRemoved(i);  // Notify the adapter that an item was removed
            }
        }
        notifyDataSetChanged();  // Notify the adapter that the data set has changed
    }


}