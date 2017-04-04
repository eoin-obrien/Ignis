package io.videtur.ignis.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.videtur.ignis.model.Chat;

import static io.videtur.ignis.util.Constants.CHAT_KEY_DELIMITER;

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
            key = userKey1 + CHAT_KEY_DELIMITER + userKey2;
        } else {
            key = userKey2 + CHAT_KEY_DELIMITER + userKey1;
        }
        return key;
    }

    public static String formatLastOnlineTime(long lastOnlineMilliseconds) {
        String formattedLastOnlineTime;
        Calendar now = Calendar.getInstance();
        Calendar lastOnline = Calendar.getInstance();
        lastOnline.setTimeInMillis(lastOnlineMilliseconds);

        long difference = now.getTimeInMillis() - lastOnline.getTimeInMillis();
        long daysDifference = TimeUnit.MILLISECONDS.toDays(difference);
        boolean sameYear = now.get(Calendar.YEAR) == lastOnline.get(Calendar.YEAR);
        boolean sameDay = sameYear && now.get(Calendar.DAY_OF_YEAR) == lastOnline.get(Calendar.DAY_OF_YEAR);
        boolean sameWeek = daysDifference < 7;

        if (sameDay) {
            SimpleDateFormat formatter = new SimpleDateFormat("'last seen at' HH:mm");
            formattedLastOnlineTime = formatter.format(lastOnline.getTime());
        } else if (sameWeek) {
            SimpleDateFormat formatter = new SimpleDateFormat("'last seen' EEE 'at' HH:mm");
            formattedLastOnlineTime = formatter.format(lastOnline.getTime());
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("'last seen' dd.MM.yy 'at' HH:mm");
            formattedLastOnlineTime = formatter.format(lastOnline.getTime());
        }
        return formattedLastOnlineTime;
    }

    public static String formatMessageTimestamp(long timestampMs) {
        String formattedLastOnlineTime;
        Calendar now = Calendar.getInstance();
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(timestampMs);

        long difference = now.getTimeInMillis() - timestamp.getTimeInMillis();
        long daysDifference = TimeUnit.MILLISECONDS.toDays(difference);
        boolean sameYear = now.get(Calendar.YEAR) == timestamp.get(Calendar.YEAR);
        boolean sameDay = sameYear && now.get(Calendar.DAY_OF_YEAR) == timestamp.get(Calendar.DAY_OF_YEAR);
        boolean sameWeek = daysDifference < 7;

        if (sameDay) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            formattedLastOnlineTime = formatter.format(timestamp.getTime());
        } else if (sameWeek) {
            SimpleDateFormat formatter = new SimpleDateFormat("EEE HH:mm");
            formattedLastOnlineTime = formatter.format(timestamp.getTime());
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy HH:mm");
            formattedLastOnlineTime = formatter.format(timestamp.getTime());
        }
        return formattedLastOnlineTime;
    }

    public static String formatChatTimestamp(long timestampMs) {
        String formattedLastOnlineTime;
        Calendar now = Calendar.getInstance();
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(timestampMs);

        long difference = now.getTimeInMillis() - timestamp.getTimeInMillis();
        long daysDifference = TimeUnit.MILLISECONDS.toDays(difference);
        boolean sameYear = now.get(Calendar.YEAR) == timestamp.get(Calendar.YEAR);
        boolean sameDay = sameYear && now.get(Calendar.DAY_OF_YEAR) == timestamp.get(Calendar.DAY_OF_YEAR);
        boolean sameWeek = daysDifference < 7;

        if (sameDay) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            formattedLastOnlineTime = formatter.format(timestamp.getTime());
        } else if (sameWeek) {
            SimpleDateFormat formatter = new SimpleDateFormat("EEE");
            formattedLastOnlineTime = formatter.format(timestamp.getTime());
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM d");
            formattedLastOnlineTime = formatter.format(timestamp.getTime());
        }
        return formattedLastOnlineTime;
    }

    public static float dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

}
