package com.fyp.musclefatigue.helpers;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {
    public static boolean isValidPassword(final CharSequence password) {

        Pattern pattern;
        Matcher matcher;
//        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-zA-Z]).{6,40}$";
//        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=_!])(?=\\S+$).{8,}$";
        final String PASSWORD_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,16}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }



    public static boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}
