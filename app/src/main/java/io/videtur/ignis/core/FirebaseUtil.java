package io.videtur.ignis.core;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import io.videtur.ignis.model.Chat;
import io.videtur.ignis.model.Message;

import static io.videtur.ignis.core.Constants.CHATS_CHILD;
import static io.videtur.ignis.core.Constants.CHATS_REF;
import static io.videtur.ignis.core.Constants.LAST_MESSAGE_CHILD;
import static io.videtur.ignis.core.Constants.MESSAGES_REF;
import static io.videtur.ignis.core.Constants.READ_RECEIPTS_CHILD;
import static io.videtur.ignis.core.Constants.UNDELIVERED_CHILD;
import static io.videtur.ignis.core.Constants.UNREAD_CHILD;
import static io.videtur.ignis.core.Constants.USERS_REF;

/**
 * Utility methods for interacting with the Firebase Database.
 */
public class FirebaseUtil {

    /**
     * Creates a new chat between two users.
     */
    public static Task<Void> createChat(FirebaseDatabase database, String chatKey,
                                        String userKey, String contactKey) {
        Map<String, Object> chatMembers = new HashMap<>();
        chatMembers.put(userKey, Boolean.TRUE);
        chatMembers.put(contactKey, Boolean.TRUE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + CHATS_REF + "/" + chatKey, new Chat(chatMembers));
        updates.put("/" + USERS_REF + "/" + userKey + "/" + CHATS_CHILD + "/" + chatKey, ServerValue.TIMESTAMP);
        updates.put("/" + USERS_REF + "/" + contactKey + "/" + CHATS_CHILD + "/" + chatKey, ServerValue.TIMESTAMP);

        return database.getReference().updateChildren(updates);
    }

    /**
     * Sends a chat message.
     */
    public static void sendMessage(FirebaseDatabase database, Message message, Chat chat,
                                   String chatKey, String userKey) {
        DatabaseReference messagesRef = database.getReference(MESSAGES_REF).child(chatKey);
        String messageKey = messagesRef.push().getKey();

        // Set last message details
        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + MESSAGES_REF + "/" + chatKey + "/" + messageKey, message);
        updates.put("/" + CHATS_REF + "/" + chatKey + "/" + LAST_MESSAGE_CHILD, messageKey);
        for (String memberKey : chat.getMembers().keySet()) {
            // Update chat activity timestamp
            updates.put("/" + USERS_REF + "/" + memberKey + "/" + CHATS_CHILD + "/" + chatKey, ServerValue.TIMESTAMP);
            // Don't track read receipts for the sender
            if (!memberKey.equals(userKey)) {
                updates.put("/" + USERS_REF + "/" + memberKey + "/" + UNDELIVERED_CHILD + "/" + chatKey + "/" + messageKey, Boolean.TRUE);
                updates.put("/" + USERS_REF + "/" + memberKey + "/" + UNREAD_CHILD + "/" + chatKey + "/" + messageKey, Boolean.TRUE);
            }
        }
        database.getReference().updateChildren(updates);
    }

    /**
     * Adds a read receipt to the given message.
     */
    public static void markMessageAsRead(FirebaseDatabase database, String userKey, String chatKey, String messageKey) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + USERS_REF + "/" + userKey + "/" + UNREAD_CHILD + "/" + chatKey + "/" + messageKey, null);
        updates.put("/" + MESSAGES_REF + "/" + chatKey + "/" + messageKey + "/" + READ_RECEIPTS_CHILD + "/" + userKey, ServerValue.TIMESTAMP);
        database.getReference().updateChildren(updates);
    }

}
