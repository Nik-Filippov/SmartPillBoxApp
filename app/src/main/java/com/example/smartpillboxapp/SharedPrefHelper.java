package com.example.smartpillboxapp;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPrefHelper {


    private static final String PREF_NAME = "CalendarPrefs";
    private static final String KEY_DAYS_IN_MONTH = "daysInMonth";
    private static final String KEY_REMINDER_DATES = "reminderDates";
    private static final String KEY_MONTH = "currentMonth";
    private static final String KEY_YEAR = "currentYear";
    private static final String KEY_PILL_ITEMS = "pillItems";

    public static void saveCalendarData(Context context, List<String> daysInMonth, ArrayList<String> reminderDates, int month, int year) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Convert List to Set<String> (for SharedPreferences compatibility)
        Set<String> daysSet = new HashSet<>(daysInMonth);

        Log.d("saveCalenderData", "reminderDates: " + reminderDates);

        // Convert reminderDates ArrayList to a single CSV string
        String reminderDatesString = String.join(",", reminderDates);

        // Save data
        editor.putStringSet(KEY_DAYS_IN_MONTH, daysSet);
        editor.putString(KEY_REMINDER_DATES, reminderDatesString);  // FIX: Use putString instead of putStringSet
        editor.putInt(KEY_MONTH, month);
        editor.putInt(KEY_YEAR, year);
        editor.apply();
    }


    public static ArrayList<String> getDaysInMonth(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> daysSet = sharedPreferences.getStringSet(KEY_DAYS_IN_MONTH, new HashSet<>());
        return new ArrayList<>(daysSet);  // Convert Set to ArrayList
    }

    public static ArrayList<String> getReminderDates(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String reminderDatesString = sharedPreferences.getString(KEY_REMINDER_DATES, null);  // FIX: Use null instead of ""

        ArrayList<String> reminderDates = new ArrayList<>();
        if (reminderDatesString != null && !reminderDatesString.isEmpty()) {
            // Split the stored string by commas to reconstruct the ArrayList
            String[] datesArray = reminderDatesString.split(",");
            for (String date : datesArray) {
                reminderDates.add(date.trim());
            }
        }

        return reminderDates;
    }
    public static int getCurrentMonth(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_MONTH, -1);
    }

    public static int getCurrentYear(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_YEAR, -1);
    }

    public static void savePillItems(Context context, ArrayList<Pill> pillItems) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (Pill pill : pillItems) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("pillName", pill.getPillName());
                jsonObject.put("pillAmount", pill.getPillAmount());
                jsonObject.put("pillTime", pill.getPillTime());
                jsonObject.put("pillRecurrence", pill.getPillRecurrence());
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        editor.putString(KEY_PILL_ITEMS, jsonArray.toString()); // Save as JSON string
        editor.apply();
    }

    public static ArrayList<Pill> getPillItems(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(KEY_PILL_ITEMS, "[]"); // Default to empty JSON array

        ArrayList<Pill> pillList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Pill pill = new Pill(
                        jsonObject.getString("pillName"),
                        jsonObject.getString("pillAmount"),
                        jsonObject.getString("pillTime"),
                        jsonObject.getString("pillRecurrence")
                );
                pillList.add(pill);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pillList;
    }
}
