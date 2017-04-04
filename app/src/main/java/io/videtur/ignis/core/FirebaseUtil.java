package io.videtur.ignis.core;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import io.videtur.ignis.model.Chat;
import io.videtur.ignis.model.Message;

public class FirebaseUtil {

    public static Task<Void> createChat(FirebaseDatabase database, String chatKey,
                                        String userKey, String contactKey) {
        Map<String, Object> chatMembers = new HashMap<>();
        chatMembers.put(userKey, Boolean.TRUE);
        chatMembers.put(contactKey, Boolean.TRUE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + Constants.CHATS_REF + "/" + chatKey, new Chat(chatMembers));
        updates.put("/" + Constants.USERS_REF + "/" + userKey + "/" + Constants.CHATS_CHILD + "/" + chatKey, ServerValue.TIMESTAMP);
        updates.put("/" + Constants.USERS_REF + "/" + contactKey + "/" + Constants.CHATS_CHILD + "/" + chatKey, ServerValue.TIMESTAMP);

        return database.getReference().updateChildren(updates);
    }

    public static void sendMessage(FirebaseDatabase database, Message message, Chat chat,
                                   String chatKey, String userKey) {
        DatabaseReference messagesRef = database.getReference(Constants.MESSAGES_REF).child(chatKey);
        String messageKey = messagesRef.push().getKey();

        // set last message details
        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + Constants.MESSAGES_REF + "/" + chatKey + "/" + messageKey, message);
        updates.put("/" + Constants.CHATS_REF + "/" + chatKey + "/" + Constants.LAST_MESSAGE_CHILD, messageKey);
        for (String memberKey : chat.getMembers().keySet()) {
            // Update chat activity timestamp
            updates.put("/" + Constants.USERS_REF + "/" + memberKey + "/" + Constants.CHATS_CHILD + "/" + chatKey, ServerValue.TIMESTAMP);
            // Don't track read receipts for the sender
            if (!memberKey.equals(userKey)) {
                updates.put("/" + Constants.USERS_REF + "/" + memberKey + "/" + Constants.UNREAD_CHILD + "/" + chatKey + "/" + messageKey, Boolean.TRUE);
            }
        }
        database.getReference().updateChildren(updates);
    }

    public static void markMessageAsRead(FirebaseDatabase database, String userKey, String chatKey, String messageKey) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + Constants.USERS_REF + "/" + userKey + "/" + Constants.UNREAD_CHILD + "/" + chatKey + "/" + messageKey, null);
        updates.put("/" + Constants.MESSAGES_REF + "/" + chatKey + "/" + messageKey + "/" + Constants.READ_RECEIPTS_CHILD + "/" + userKey, ServerValue.TIMESTAMP);
        database.getReference().updateChildren(updates);
    }

}
