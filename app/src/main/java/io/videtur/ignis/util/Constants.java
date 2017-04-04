package io.videtur.ignis.util;

public final class Constants {

    public static final String RESTART_BROADCAST = "io.videtur.ignis.service.RestartNotificationService";

    // Notification settings
    public static final int NOTIFICATION_ID = 1;
    public static final int LED_COLOR = 0xff2196f3;

    // Firebase database paths
    public static final String CONNECTED_REF = ".info/connected";
    public static final String CHATS_REF = "chats";
    public static final String CONTACTS_REF = "contacts";
    public static final String MESSAGES_REF = "messages";
    public static final String USERS_REF = "users";

    // Firebase database children
    public static final String CONNECTIONS_CHILD = "connections";
    public static final String LAST_ONLINE_CHILD = "lastOnline";
    public static final String CHATS_CHILD = "chats";
    public static final String LAST_MESSAGE_CHILD = "lastMessage";
    public static final String UNREAD_CHILD = "unread";
    public static final String READ_RECEIPTS_CHILD = "readReceipts";
    public static final String DELIVERY_RECEIPTS_CHILD = "deliveryReceipts";

    // Item types for chat recycler
    public static final int MESSAGE_FROM_USER = 0;
    public static final int MESSAGE_TO_USER = 1;

    // Activity request codes
    public static final int REQUEST_INVITE = 100;
    public static final int REQUEST_SIGN_IN = 101;

    public static final String CHAT_KEY_DELIMITER = ",";

    private Constants() {
    }

}
