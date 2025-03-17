package com.example.smartpillboxapp;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import java.text.DateFormat;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    public interface TimePickerListener {
        void onTimeSelected(String time);
    }

    private TimePickerListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // Use current time as default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, false);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Convert hour to 12-hour format
        int hour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
        String amPm = (hourOfDay < 12) ? "AM" : "PM";

        // Format minute without leading zero
        String time = hour + ":" + (minute < 10 ? "0" + minute : minute) + " " + amPm;


        // Ensure the listener is not null before calling it
        if (listener != null) {
            listener.onTimeSelected(time);
        }
    }

    public void setTimePickerListener(TimePickerListener listener) {
        this.listener = listener;
    }
}
