package com.example.smartpillboxapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    // Interface to pass selected date to the activity
    public interface DatePickerListener {
        void onDateSelected(String date);
    }

    private DatePickerListener listener;
    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState){
        // Use current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Convert month number to month name (0 = January, 1 = February, etc.)
        String monthName = new DateFormatSymbols().getMonths()[month];

        // Format the date as "February 12, 2025"
        String formattedDate = monthName + " " + dayOfMonth + ", " + year;

        // Pass the formatted date to the listener
        if (listener != null) {
            listener.onDateSelected(formattedDate);
        }
    }

    // Method to set the listener
    public void setDatePickerListener(DatePickerListener listener) {
        this.listener = listener;
    }
}
