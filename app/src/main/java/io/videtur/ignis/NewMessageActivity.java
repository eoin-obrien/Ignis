package io.videtur.ignis;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseIndexListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.videtur.ignis.model.Chat;
import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;

import static io.videtur.ignis.util.Constants.CHAT_REF;
import static io.videtur.ignis.util.Constants.CONTACTS_REF;
import static io.videtur.ignis.util.Constants.USERS_REF;
import static io.videtur.ignis.util.Util.formatLastOnlineTime;
import static io.videtur.ignis.util.Util.generateChatKey;

public class NewMessageActivity extends IgnisAuthActivity {

    private static final String TAG = "NewMessageActivity";

    private DatabaseReference mContactsRef;
    private DatabaseReference mContactsKeyRef;
    private DatabaseReference mChatsRef;
    private DatabaseReference mUsersRef;
    private FirebaseIndexListAdapter<User> mContactsAdapter;
    private TextWatcher mSearchTextWatcher;

    private EditText mSearchEditText;
    private ListView mContactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSearchEditText = (EditText) findViewById(R.id.contacts_search_edit_text);
        mContactsList = (ListView) findViewById(R.id.contacts_list);

        mContactsList.setEmptyView(findViewById(R.id.empty_search));

        // Setup database references
        mContactsRef = getDatabase().getReference(CONTACTS_REF);
        mChatsRef = getDatabase().getReference(CHAT_REF);
        mUsersRef = getDatabase().getReference(USERS_REF);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onUserDataChange(final String key, final User user) {
        super.onUserDataChange(key, user);

        mContactsKeyRef = mContactsRef.child(key);
        setContactsAdapter();
        mContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO generate chat if it doesn't exist
                final String contactKey = mContactsAdapter.getRef(position).getKey();
                final User contact = mContactsAdapter.getItem(position);
                final String chatKey = generateChatKey(key, contactKey);
                mChatsRef.child(chatKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Map<String, Object> chatNames = new HashMap<>();
                            chatNames.put(key, contact.getName());
                            chatNames.put(contactKey, user.getName());
                            Map<String, Object> chatProfilePhotos = new HashMap<>();
                            chatProfilePhotos.put(key, contact.getPhotoUrl());
                            chatProfilePhotos.put(contactKey, user.getPhotoUrl());
                            mChatsRef.child(chatKey)
                                    .setValue(new Chat(chatNames, chatProfilePhotos))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            startChatActivity(chatKey);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showToast(R.string.chat_creation_failed);
                                        }
                                    });
                        } else {
                            startChatActivity(chatKey);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        if (mSearchTextWatcher != null) {
            mSearchEditText.removeTextChangedListener(mSearchTextWatcher);
        }
        mSearchTextWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO decide on minimum search term length
                String searchTerm = mSearchEditText.getText().toString().toLowerCase();
                setContactsAdapter(searchTerm);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        mSearchEditText.addTextChangedListener(mSearchTextWatcher);
    }

    private void startChatActivity(String chatKey) {
        Intent intent = new Intent(NewMessageActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.ARG_CHAT_KEY, chatKey);
        startActivity(intent);
        finish();
    }

    private void setContactsAdapter() {
        setContactsAdapter("");
    }

    private void setContactsAdapter(String searchTerm) {
        Query keyRef = mContactsKeyRef.orderByValue().startAt(searchTerm).endAt(searchTerm + "~");
        mContactsAdapter = new FirebaseIndexListAdapter<User>(this, User.class, R.layout.list_item_contact, keyRef, mUsersRef) {
            @Override
            protected void populateView(View v, User model, int position) {
                ImageView contactPhoto = (ImageView) v.findViewById(R.id.contact_profile_image);
                TextView contactName = (TextView) v.findViewById(R.id.contact_user_name);
                TextView contactStatus = (TextView) v.findViewById(R.id.contact_user_status);
                Glide.with(NewMessageActivity.this).load(model.getPhotoUrl()).fitCenter().into(contactPhoto);
                contactName.setText(model.getName());
                if (model.getConnections() != null && model.getConnections().size() > 0) {
                    contactStatus.setText("online");
                } else {
                    contactStatus.setText(formatLastOnlineTime(model.getLastOnline()));
                }
            }

        };
        mContactsList.setAdapter(mContactsAdapter);
    }

}
