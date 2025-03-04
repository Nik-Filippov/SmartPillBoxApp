package com.example.smartpillboxapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;

public class PillListViewHolder extends RecyclerView.ViewHolder {
    public TextView pillNameTextView, pillAmountTextView, pillTimeTextView;

    public PillListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.pillNameTextView = itemView.findViewById(R.id.pillNameTextView);
        this.pillAmountTextView = itemView.findViewById(R.id.pillAmountTextView);
        this.pillTimeTextView = itemView.findViewById(R.id.pillTimeTextView);
    }
}
