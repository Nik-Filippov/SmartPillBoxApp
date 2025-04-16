package com.example.smartpillboxapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;

public class PillSupplyCheckReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PillSupplyCheckReceiver", "Checking pill supply...");

        DatabaseHelper dbHelper = new DatabaseHelper(context, "PillReminderDatabase", null, 1);
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        NotificationHelper notificationHelper = new NotificationHelper();

        int[] containerNumbers = new int[3];
        boolean sendNotification = false;
        Integer[] numPills = getCurrentPillCounts(context);
        for (int containerNumber = 1; containerNumber <= 3; containerNumber++) {
            int threshold = getThreshold(sqLiteDatabase, containerNumber);
            Log.d("PillSupplyChecker", "Threshold: " + threshold);
            if (numPills[containerNumber - 1] < threshold) {
                containerNumbers[containerNumber - 1] = containerNumber;
                sendNotification = true;
            } else {
                containerNumbers[containerNumber - 1] = -1;
            }
        }

        if (sendNotification) {
            notificationHelper.sendRefillNotification(context, containerNumbers);
        }

        sqLiteDatabase.close();

        // 🔁 Schedule next day's alarm
        scheduleNextDailyAlarm(context);
    }

    private int getThreshold(SQLiteDatabase db, int containerNumber) {
        String query = "SELECT reminderThreshold FROM PillReminder WHERE container = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(containerNumber)});
        int threshold = -1;
        if (cursor.moveToNext()) {
            threshold = cursor.getInt(0);
        }
        cursor.close();
        return threshold;
    }

    private Integer[] getCurrentPillCounts(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SubtitlePrefs", Context.MODE_PRIVATE);
        Integer[] numPills = new Integer[3];
        numPills[0] = sharedPreferences.getInt("pill_count_1", -1);
        numPills[1] = sharedPreferences.getInt("pill_count_2", -1);
        numPills[2] = sharedPreferences.getInt("pill_count_3", -1);
        return numPills;
    }

    private void scheduleNextDailyAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PillSupplyCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Always set for tomorrow

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
            Log.d("PillSupplyCheckReceiver", "Next daily check scheduled for: " + calendar.getTime());
        }
    }

}
