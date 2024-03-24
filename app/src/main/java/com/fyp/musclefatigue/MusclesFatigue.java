package com.fyp.musclefatigue;

import static com.fyp.musclefatigue.helpers.Constants.PRIMARY_CHANNEL_ID;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;

import java.util.Locale;

public class MusclesFatigue extends Application {

    private static final String TAG = "MusclesFatigue";

    private Locale mSystemLocale;
    private static MusclesFatigue mInstance;


    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        mSystemLocale = Locale.getDefault();

        createNotificationChannel(
                getString(R.string.app_name),
                "This channel will keep you updated with the latest features, news, tips and offers from our app." +
                        " You can customize your notification preferences in the app settings. To unsubscribe," +
                        " long press on the notification and select \"Turn off notifications\"." +
                        " Thank you for using our app! .",
                PRIMARY_CHANNEL_ID);


    }


    public static synchronized MusclesFatigue getInstance() {
        return mInstance;
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    private void createNotificationChannel(String name, String description, String channelId) {

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        Uri notificationSoundUri = Uri.parse("android.resource://com.fyp.musclefatigue/" + R.raw.workout);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.enableLights(true);
            channel.setSound(notificationSoundUri,audioAttributes);
            channel.setLightColor(1682023); // app primary color
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
