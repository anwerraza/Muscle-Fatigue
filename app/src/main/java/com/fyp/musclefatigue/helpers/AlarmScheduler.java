package com.fyp.musclefatigue.helpers;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fyp.musclefatigue.AlarmReceiver;


public class AlarmScheduler {

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleAlarm(Context context,int requestCode, String time, String workout, long alarmTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent notifyIntent = new Intent(context, AlarmReceiver.class);
        notifyIntent.putExtra("notification_id",requestCode);
        notifyIntent.putExtra("workout",workout);
        notifyIntent.putExtra("time",time);
        if (alarmManager != null) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
        }
    }

    public static void cancelAlarm(Context context,int requestCode, String time, String workout) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent notifyIntent = new Intent(context, AlarmReceiver.class);
        notifyIntent.putExtra("notification_id",requestCode);
        notifyIntent.putExtra("workout",workout);
        notifyIntent.putExtra("time",time);
        if (alarmManager != null) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            alarmManager.cancel(pendingIntent);
        }
    }
}
