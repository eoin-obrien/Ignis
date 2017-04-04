package io.videtur.ignis.util;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import io.videtur.ignis.model.Chat;

import static io.videtur.ignis.util.Constants.CHATS_CHILD;
import static io.videtur.ignis.util.Constants.CHATS_REF;
import static io.videtur.ignis.util.Constants.USERS_REF;

public class FirebaseUtil {

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

}
