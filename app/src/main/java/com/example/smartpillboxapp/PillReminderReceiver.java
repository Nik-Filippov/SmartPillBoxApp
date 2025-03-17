package com.example.smartpillboxapp;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PillReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String pillName = intent.getStringExtra("pill_name");
        String pillAmount = intent.getStringExtra("pill_amount");


        NotificationHelper.sendNotification(context, pillName, pillAmount);
    }
}
