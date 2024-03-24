package com.fyp.musclefatigue.helpers;

import android.util.Log;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by M.Sufwan Ansari on 11/3/2021.
 * Copyright (c) 2021 MTPixels. All rights reserved.
 */
public class TimestampUtil {

    public static long getNextWeekAlarmTime(String time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return dateTimeToMillis(s.format(calendar.getTime())+" "+time);
    }

    public static String getFutureDateByDayName(String day) {
        String daysList[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(new Date());
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        for (int i = 0; i <= daysList.length; i++) {
            String currentDay = new SimpleDateFormat("EEEE", Locale.US).format(calendar.getTime()).toString();
            Log.d("TimestampUtil", "onSetAlarm current day:"+currentDay);
            if (currentDay.equalsIgnoreCase(day))
                break;
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }
        Log.d("TimestampUtil", "onSetAlarm find date by day:"+s.format(calendar.getTime()));
        return s.format(calendar.getTime());
    }

    public static String getFutureDateByDay(String day) {
        String[] daysList = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        Calendar calendar = Calendar.getInstance();
        int currentDayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        int targetDayIndex = Arrays.asList(daysList).indexOf(day);

        // Check if the target day is the same as the current day
        if (targetDayIndex == currentDayIndex - 1)
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());

        int daysToAdd = targetDayIndex >= currentDayIndex ? targetDayIndex - currentDayIndex : (7 - currentDayIndex) + targetDayIndex;

        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return dateFormat.format(calendar.getTime());
    }

    public static String getMillisToClockTimeString(long millis) {

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        long _hours = seconds / 3600;
        long _minutes = (seconds % 3600) / 60;
        long _secs = seconds % 60;

        // Format the seconds into hours, minutes,
        // and seconds.
        if (_hours != 0)
            return String.format(Locale.getDefault(), "%d:%02d:%02d", _hours, _minutes, _secs);
        else
            return String.format(Locale.getDefault(), "%02d:%02d", _minutes, _secs);
    }



    public static long dateTimeToMillis(String inputDate){
        if (inputDate == null || inputDate.isEmpty() || inputDate.equals("0")){
            return 0L;
        }else{
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a",Locale.US);
                Date parsedDate = inputFormat.parse(inputDate);
                return parsedDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                return 0L;
            }
        }
    }

    public static String formattedTime(long time) {
        if (time == 0) {
            return "";
        } else {
            Calendar message_cal = Calendar.getInstance(Locale.ENGLISH);
            message_cal.setTimeInMillis(time);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);
            return df.format(message_cal.getTime());
        }
    }

}
