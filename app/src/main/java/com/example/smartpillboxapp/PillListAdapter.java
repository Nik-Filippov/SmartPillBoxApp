package com.example.smartpillboxapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PillListAdapter extends RecyclerView.Adapter<PillListViewHolder> {
    private final ArrayList<Pill> pillList;

    public PillListAdapter(ArrayList<Pill> pillNames) {
        this.pillList = pillNames;
    }

    @NonNull
    @Override
    public PillListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pill_list_item, parent, false);
        return new PillListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PillListViewHolder holder, int position) {
        Pill pill = pillList.get(position);
//        holder.pillListTextView.setText(pillName);
        holder.pillNameTextView.setText(pill.getPillName());
        holder.pillAmountTextView.setText(pill.getPillAmount());
        holder.pillTimeTextView.setText(pill.getPillTime());
    }

    @Override
    public int getItemCount() {
        return pillList.size();
    }
}
