package com.example.smartpillboxapp;

import static java.lang.Double.NaN;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.round;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment{
    private TextView tvDateTime;
    private TextView tvData1, tvData2, tvData3;
    private TextView etData1Subtitle, etData2Subtitle, etData3Subtitle;
    private Button btnEdit1, btnEdit2, btnEdit3;
    private SharedViewModel sharedViewModel;
    private int edited_container_number = 0;
    private double onePillWeightContainer1 = NaN;
    private double onePillWeightContainer2 = NaN;
    private double onePillWeightContainer3 = NaN;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SubtitlePrefs";
    private static final String KEY_SUBTITLE_1 = "subtitle1";
    private static final String KEY_SUBTITLE_2 = "subtitle2";
    private static final String KEY_SUBTITLE_3 = "subtitle3";
    private static final String KEY_ONE_PILL_1 = "one_pill_weight_1";
    private static final String KEY_ONE_PILL_2 = "one_pill_weight_2";
    private static final String KEY_ONE_PILL_3 = "one_pill_weight_3";
    private SQLiteDatabase sqLiteDatabase;
    private DatabaseHelper dbHelper;
    private Integer[] numPills;
    private Integer[] testNumPills;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private NotificationHelper notificationHelper;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        tvDateTime = view.findViewById(R.id.tvDateTime);

        tvData1 = view.findViewById(R.id.tvData1);
        tvData2 = view.findViewById(R.id.tvData2);
        tvData3 = view.findViewById(R.id.tvData3);

        btnEdit1 = view.findViewById(R.id.btnEdit1);
        btnEdit2 = view.findViewById(R.id.btnEdit2);
        btnEdit3 = view.findViewById(R.id.btnEdit3);

        etData1Subtitle = view.findViewById(R.id.etData1Subtitle);
        etData2Subtitle = view.findViewById(R.id.etData2Subtitle);
        etData3Subtitle = view.findViewById(R.id.etData3Subtitle);

        // Load saved subtitles
        etData1Subtitle.setText(sharedPreferences.getString(KEY_SUBTITLE_1, ""));
        etData2Subtitle.setText(sharedPreferences.getString(KEY_SUBTITLE_2, ""));
        etData3Subtitle.setText(sharedPreferences.getString(KEY_SUBTITLE_3, ""));

        onePillWeightContainer1 = parseDoubleOrDefault(sharedPreferences.getString(KEY_ONE_PILL_1, ""), NaN);
        onePillWeightContainer2 = parseDoubleOrDefault(sharedPreferences.getString(KEY_ONE_PILL_2, ""), NaN);
        onePillWeightContainer3 = parseDoubleOrDefault(sharedPreferences.getString(KEY_ONE_PILL_3, ""), NaN);

        updateDateTime();
        scheduleDailyCheck();
        notificationHelper.createPillRefillChannel(this.getContext());

        // "Edit" buttons' click listeners
        btnEdit1.setOnClickListener(v -> {
            edited_container_number = 1;
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("container_number", String.valueOf(edited_container_number));
            args.putString("current_subtitle", etData1Subtitle.getText().toString());
            args.putString("one_pill_weight", String.valueOf(onePillWeightContainer1));
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");

//             TESTING
//            testNumPills[0] = testNumPills[0] - 1;
//            try {
//                updateDataFields(testNumPills);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//             END TESTING
        });

        btnEdit2.setOnClickListener(v -> {
            edited_container_number = 2;
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("container_number", String.valueOf(edited_container_number));
            args.putString("current_subtitle", etData2Subtitle.getText().toString());
            args.putString("one_pill_weight", String.valueOf(onePillWeightContainer2));
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");

            //             TESTING
//            testNumPills[0] = testNumPills[0] + 1;
//            try {
//                updateDataFields(testNumPills);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//             END TESTING
        });

        btnEdit3.setOnClickListener(v -> {
            edited_container_number = 3;
            EditContainerInfoDialog dialogFragment = new EditContainerInfoDialog();
            Bundle args = new Bundle();
            args.putString("container_number", String.valueOf(edited_container_number));
            args.putString("current_subtitle", etData3Subtitle.getText().toString());
            args.putString("one_pill_weight", String.valueOf(onePillWeightContainer3));
            dialogFragment.setArguments(args);
            dialogFragment.show(getChildFragmentManager(), "EditContainerDialog");
        });

        // Retrieving data from dialog triggered by "Edit" buttons
        getChildFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
            String one_pill_weight = result.getString("new_one_pill_weight");
            String subtitle = result.getString("subtitle");
            if (edited_container_number == 1) {
                etData1Subtitle.setText(subtitle);
                onePillWeightContainer1 = Double.parseDouble(one_pill_weight);
                saveToCache(KEY_SUBTITLE_1, subtitle);
                saveToCache(KEY_ONE_PILL_1, one_pill_weight);
            } else if (edited_container_number == 2) {
                etData2Subtitle.setText(subtitle);
                onePillWeightContainer2 = Double.parseDouble(one_pill_weight);
                saveToCache(KEY_SUBTITLE_2, subtitle);
                saveToCache(KEY_ONE_PILL_2, one_pill_weight);
            } else if (edited_container_number == 3) {
                etData3Subtitle.setText(subtitle);
                onePillWeightContainer3 = Double.parseDouble(one_pill_weight);
                saveToCache(KEY_SUBTITLE_3, subtitle);
                saveToCache(KEY_ONE_PILL_3, one_pill_weight);
            }
        });

        // Send weight data to dialog
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // TEST NUM PILLS
//        testNumPills = new Integer[3];
//        testNumPills[0] = 20; // Container 1
//        testNumPills[1] = 12; // Container 2
//        testNumPills[2] = 8; // Container 3
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt("pill_count_1", testNumPills[0]);
//        editor.putInt("pill_count_2", testNumPills[1]);
//        editor.putInt("pill_count_3", testNumPills[2]);
//        editor.apply();  // Apply the changes to SharedPreferences
//
//
//        try {
//            updateDataFields(testNumPills);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        sharedViewModel.setNumPills(testNumPills);

        // END TESTING

        // Listen for Bluetooth data updates
        BluetoothManager bluetoothManager = BluetoothManager.getInstance(getContext());
        bluetoothManager.setDataListener(new BluetoothManager.DataListener() {
            @Override
            public void onDataReceived(String data) {
                String[] dataPartsStr = data.split(";");
                Double[] dataPartsDouble = new Double[dataPartsStr.length];
                numPills = new Integer[dataPartsStr.length];
                Double[] onePillWeights = {onePillWeightContainer1, onePillWeightContainer2, onePillWeightContainer3};
                for(int i = 0; i < dataPartsStr.length; i++){
                    double weight = Double.parseDouble(dataPartsStr[i]);
                    // Normalize weight to get accurate grams
                    if(i == 0){
                        weight *= 20/37.59;
                    } else if (i == 1){
                        weight *= 20/38.78;
                    } else if (i == 2){
                        weight *= 20/37.99;
                    }
                    dataPartsStr[i] = String.format("%.2f", weight);
                    dataPartsDouble[i] = weight;
                    if(!(Double.isNaN(onePillWeights[i]) || onePillWeights[i] == 0)){
                        //Log.e("Main",weight + "," + onePillWeights[i]);
                        numPills[i] = Math.toIntExact(round(weight / onePillWeights[i]));
                    } else {
                        numPills[i] = MIN_VALUE;
                    }
                }
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    // The line below displays weight instead of pill counts
                    // updateDataFields(dataPartsDouble);

                    try {
                        updateDataFields(numPills);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    sharedViewModel.setNumPills(numPills);
                    if(edited_container_number != 0) {
                        sharedViewModel.updateData(dataPartsStr[edited_container_number - 1]);
                    }
                });
            }

            @Override
            public void onConnected(String message) {}

            @Override
            public void onError(String error) {}
        });

        return view;
    }

    // Method to save data to SharedPreferences
    private void saveToCache(String key, String subtitle) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, subtitle);
        editor.apply();
    }

    // Helper methods
    private void updateDataFields(@NonNull Integer[] dataParts) throws InterruptedException {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve previous pill counts
        int prevCount1 = sharedPreferences.getInt("pill_count_1", -1);
        int prevCount2 = sharedPreferences.getInt("pill_count_2", -1);
        int prevCount3 = sharedPreferences.getInt("pill_count_3", -1);

        editor.putInt("prev_pill_count_1", prevCount1);
        editor.putInt("prev_pill_count_2", prevCount2);
        editor.putInt("prev_pill_count_3", prevCount3);


        if (dataParts.length == 3) {
            if (dataParts[0] != MIN_VALUE) {
                tvData1.setText(Integer.toString(dataParts[0]));
                if (prevCount1 != -1 && dataParts[0] < prevCount1){
                    waitAndCheckPillReminder(0, dataParts[0], "1");
                }
                editor.putInt("pill_count_1", dataParts[0]);
            } else {
                tvData1.setText("Set one pill weight");
            }
            if (dataParts[1] != MIN_VALUE) {
                tvData2.setText(Integer.toString(dataParts[1]));
                if (prevCount2 != -1 && dataParts[1] < prevCount2) {
                    waitAndCheckPillReminder(1, dataParts[1], "2");
                }
                editor.putInt("pill_count_2", dataParts[1]);
            } else {
                tvData2.setText("Set one pill weight");
            }
            if (dataParts[2] != MIN_VALUE) {
                tvData3.setText(Integer.toString(dataParts[2]));
                if (prevCount3 != -1 && dataParts[2] < prevCount3){
                    waitAndCheckPillReminder(2, dataParts[2], "3");
                }
                editor.putInt("pill_count_3", dataParts[2]);
            } else {
                tvData3.setText("Set one pill weight");
            }
        }
        editor.apply();
    }

    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a 'on' EEEE, MMMM dd", Locale.getDefault());
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String currentDateTime = dateFormat.format(new Date());
                String formattedDateTime = "It's " + currentDateTime;
                requireActivity().runOnUiThread(() -> tvDateTime.setText(formattedDateTime));
                handler.postDelayed(this, 1000);
            }
        });
    }

    private double parseDoubleOrDefault(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void scheduleDailyCheck() {
        Context context = requireContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PillSupplyCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to trigger at 8:00 AM every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            // If 8 AM has already passed today, schedule for tomorrow
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Set repeating alarm with exact and allow while idle
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        }

        Log.d("HomeFragment", "Daily pill supply check scheduled for 8 AM.");
    }


    private void checkForReminder(String container){
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        String query = "SELECT time, pill_name FROM PillReminder WHERE date = ? AND container = ?";

        dbHelper = new DatabaseHelper(this.getContext(), "PillReminderDatabase", null, 1);
        sqLiteDatabase = dbHelper.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{currentDate, container});
        String time = null;
        String pillName = null;
        if (cursor != null) {
            while (cursor.moveToNext()){
                time = cursor.getString(0);
                pillName = cursor.getString(1);
            }
            checkTimeAndDeleteOrNotify(currentDate, time, pillName);
            cursor.close();
        }

    }

    private void checkTimeAndDeleteOrNotify(String date, String time, String pillName) {
        if (time == null || time.trim().isEmpty()) {
            Log.e("HomeFragment", "Reminder time is null or empty. Skipping deletion.");
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
            LocalTime reminderTime = LocalTime.parse(time, formatter);
            LocalTime currentTime = LocalTime.now();

            // Check if current time is after reminder time
            if (currentTime.isAfter(reminderTime)) {
                long result = sqLiteDatabase.delete("PillReminder", "pill_name = ? AND date = ? AND time = ?", new String[]{pillName, date, time});
                if (result == -1) {
                    Log.e("Database", "Deletion failed for reminder: " + pillName + " at " + time + " on " + date);
                } else {
                    Log.d("Database", "Successfully deleted reminder: " + pillName + " at " + time + " on " + date);
                    sharedViewModel.triggerCalendarUpdate();
                }
            } else {
                Log.d("ReminderCheck", "Reminder time not passed yet. Current time: " + currentTime + ", Reminder: " + reminderTime);
            }

        } catch (Exception e) {
            Log.e("HomeFragment", "Failed to parse reminder time: " + time, e);
        }
    }

    // Wait a few seconds and recheck before executing checkForReminder
    private void waitAndCheckPillReminder(int pillIndex, int newCount, String container) {
        handler.postDelayed(() -> {
            int latestCount = sharedPreferences.getInt("pill_count_" + (pillIndex + 1), -1);
            if (latestCount == newCount) { // Pill count remains down
                Log.d("HomeFragment", "Pill found after delay, executing checkForReminder");
                checkForReminder(container);
            } else {
                Log.d("HomeFragment", "Pill count restored, no action taken");
            }
        }, 5000); // 5-second delay
    }
}
