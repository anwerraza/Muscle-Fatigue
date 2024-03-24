package com.fyp.musclefatigue;

import static com.fyp.musclefatigue.helpers.Constants.PRIMARY_CHANNEL_ID;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.fyp.musclefatigue.helpers.AlarmScheduler;
import com.fyp.musclefatigue.helpers.TimestampUtil;
import com.fyp.musclefatigue.screen.HomeActivity;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive (Context context, Intent intent) {
        Log.d(TAG, "onReceive: AlarmReceiver Class");
        int notification_id = intent.getExtras().getInt("notification_id");
        String workout = intent.getExtras().getString("workout", "");
        String time = intent.getExtras().getString("time","");
        deliverNotification(context,notification_id,workout,time);
        AlarmScheduler.scheduleAlarm(context,notification_id,time,workout, TimestampUtil.getNextWeekAlarmTime(time));
    }

    private void deliverNotification (Context context, int notification_id, String workout, String time) {
        Log.d(TAG, "deliverNotification: AlarmReceiver Class");

        Intent contentIntent = new Intent(context, HomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notification_id,
                contentIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notificationSoundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.workout);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.ic_app_logo)
                .setContentTitle("Is time to stand up")
                .setContentText("It's "+time+ " Clock \nYou should stand up and go for workout and doing some "+workout)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSound(notificationSoundUri)
                .setLights(Color.rgb(168, 20, 23), 2000, 1000)
                .setContentIntent(pendingIntent);

        // Add BigTextStyle to show full content
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText("It's "+time+ " Clock \nYou should stand up and go for workout and doing some "+workout);
        notificationBuilder.setStyle(bigTextStyle);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notification_id, notificationBuilder.build());

    }

}
