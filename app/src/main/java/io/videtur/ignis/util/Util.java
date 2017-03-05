package io.videtur.ignis.util;

import android.util.Base64;

public final class Util {

    public static String getKeyFromEmail(String email) {
        byte[] bytes = email.getBytes();
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
    }

    public static String getEmailFromKey(String key) {
        byte[] bytes = Base64.decode(key, Base64.URL_SAFE | Base64.NO_WRAP);
        return new String(bytes);
    }

}
