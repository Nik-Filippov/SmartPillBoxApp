package com.example.smartpillboxapp;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class PillReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PillReminderReceiver", "In onReceive");
        String pillName = intent.getStringExtra("pill_name");
        String pillAmount = intent.getStringExtra("pill_amount");

        NotificationHelper.sendNotification(context, pillName, pillAmount);
    }
}
