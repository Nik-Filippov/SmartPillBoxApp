package com.example.smartpillboxapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "pill_reminder_channel";
    private static final String REFILL_CHANNEL_ID = "pill_refill_channel";

    public static void createNotificationChannel(Context context){
        CharSequence name = "Pill Reminder";
        String description = "Notification for pill reminders";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    public static void sendNotification(Context context, String pillName, String pillAmount){
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Change to own icon at some point
                .setContentTitle("It's time to take your medication!")
                .setContentText(pillName + "(" + pillAmount + ")")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void createPillRefillChannel(Context context){
        CharSequence name = "Pill Refill";
        String description = "Notification for pill refills";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(REFILL_CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    public static void sendRefillNotification(Context context, int[] containerNumbers){
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        StringBuilder contentText = new StringBuilder("Please refill the following containers:\n");

        for (int i = 0; i < containerNumbers.length; i++){
            if (containerNumbers[i] != -1){
                contentText.append("Container " + (i + 1) + "\n");
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, REFILL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Change to own icon at some point
                .setContentTitle("Pill Supply Low!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText.toString()))  // Use BigTextStyle here
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

}
