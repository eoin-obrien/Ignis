package io.videtur.ignis.core;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class Util {

    public static String getKeyFromEmail(String email) {
        byte[] bytes = email.getBytes();
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
    }

    public static String getEmailFromKey(String key) {
        byte[] bytes = Base64.decode(key, Base64.URL_SAFE | Base64.NO_WRAP);
        return new String(bytes);
    }

    public static String generateChatKey(String userKey1, String userKey2) {
        String key;
        if (userKey1.compareTo(userKey2) < 0) {
            key = userKey1 + Constants.CHAT_KEY_DELIMITER + userKey2;
        } else {
            key = userKey2 + Constants.CHAT_KEY_DELIMITER + userKey1;
        }
        return key;
    }

    public static String formatTimestamp(long timestampMs, String sameDayFormat, String sameWeekFormat, String defaultFormat) {
        String formattedTime;
        Calendar now = Calendar.getInstance();
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(timestampMs);

        long difference = now.getTimeInMillis() - timestamp.getTimeInMillis();
        long daysDifference = TimeUnit.MILLISECONDS.toDays(difference);
        boolean sameYear = now.get(Calendar.YEAR) == timestamp.get(Calendar.YEAR);
        boolean sameDay = sameYear && now.get(Calendar.DAY_OF_YEAR) == timestamp.get(Calendar.DAY_OF_YEAR);
        boolean sameWeek = daysDifference < 7;

        if (sameDay) {
            formattedTime = (String) DateFormat.format(sameDayFormat, timestamp);
        } else if (sameWeek) {
            formattedTime = (String) DateFormat.format(sameWeekFormat, timestamp);
        } else {
            formattedTime = (String) DateFormat.format(defaultFormat, timestamp);
        }
        return formattedTime;
    }

    public static float dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

}
