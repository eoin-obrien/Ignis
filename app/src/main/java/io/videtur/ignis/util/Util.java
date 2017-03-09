package io.videtur.ignis.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Base64;

import java.text.SimpleDateFormat;
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

}
