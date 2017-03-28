package io.videtur.ignis.util;

public final class Constants {

    // Firebase database paths
    public static final String CONNECTED_REF = ".info/connected";
    public static final String CONTACTS_REF = "contacts";
    public static final String USERS_REF = "users";
    public static final String CHAT_REF = "chats";
    public static final String MESSAGES_REF = "messages";

    // Firebase database children
    public static final String CONNECTIONS_CHILD = "connections";
    public static final String LAST_ONLINE_CHILD = "lastOnline";

    // Item types for chat recycler
    public static final int MESSAGE_FROM_USER = 0;
    public static final int MESSAGE_TO_USER = 1;

    public static final String CHAT_KEY_DELIMITER = ",";

    private Constants() {
    }

}
