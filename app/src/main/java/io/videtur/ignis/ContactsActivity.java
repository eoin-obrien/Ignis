package io.videtur.ignis;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;

import static io.videtur.ignis.util.Constants.CHATS_REF;
import static io.videtur.ignis.util.Constants.CONTACTS_REF;
import static io.videtur.ignis.util.Constants.USERS_REF;
import static io.videtur.ignis.util.FirebaseUtil.createChat;
import static io.videtur.ignis.util.Util.formatTimestamp;
import static io.videtur.ignis.util.Util.generateChatKey;

public class ContactsActivity extends IgnisAuthActivity {

    private static final String TAG = "ContactsActivity";

    private DatabaseReference mChatsRef;
    private DatabaseReference mContactsRef;
    private DatabaseReference mContactsKeyRef;
    private DatabaseReference mUsersRef;
    private FirebaseIndexListAdapter<User> mContactsAdapter;
    private TextWatcher mSearchTextWatcher;

    private EditText mSearchEditText;
    private ListView mContactsList;
    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSearchEditText = (EditText) findViewById(R.id.contacts_search_edit_text);
        mContactsList = (ListView) findViewById(R.id.contacts_list);

        // TODO change to loading spinner
        mContactsList.setEmptyView(findViewById(R.id.empty_search));

        // Setup database references
        mChatsRef = getDatabase().getReference(CHATS_REF);
        mContactsRef = getDatabase().getReference(CONTACTS_REF);
        mUsersRef = getDatabase().getReference(USERS_REF);

        mResources = getResources();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_contact:
                startActivity(new Intent(this, AddContactActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onUserDataChange(final String key, User user) {
        super.onUserDataChange(key, user);

        mContactsKeyRef = mContactsRef.child(key);
        setContactsAdapter("");
        mContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // generate chat if it doesn't exist
                final String contactKey = mContactsAdapter.getRef(position).getKey();
                final String chatKey = generateChatKey(key, contactKey);
                mChatsRef.child(chatKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            createChat(getDatabase(), chatKey, key, contactKey)
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
                        if (databaseError != null) {
                            Log.e(TAG, databaseError.getMessage());
                        }
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
        Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.ARG_CHAT_KEY, chatKey);
        startActivity(intent);
        finish();
    }

    private void setContactsAdapter(String searchTerm) {
        Query keyRef = mContactsKeyRef.orderByValue().startAt(searchTerm).endAt(searchTerm + "~");
        mContactsAdapter = new FirebaseIndexListAdapter<User>(this, User.class, R.layout.list_item_contact, keyRef, mUsersRef) {
            @Override
            protected void populateView(View v, User model, int position) {
                ImageView contactPhoto = (ImageView) v.findViewById(R.id.contact_profile_image);
                TextView contactName = (TextView) v.findViewById(R.id.contact_user_name);
                TextView contactStatus = (TextView) v.findViewById(R.id.contact_user_status);
                Glide.with(ContactsActivity.this).load(model.getPhotoUrl()).fitCenter().into(contactPhoto);
                contactName.setText(model.getName());
                if (model.getConnections() != null && model.getConnections().size() > 0) {
                    contactStatus.setText(getResources().getString(R.string.user_online));
                } else {
                    contactStatus.setText(formatTimestamp(model.getLastOnline(),
                            mResources.getString(R.string.last_online_timestamp_same_day),
                            mResources.getString(R.string.last_online_timestamp_same_week),
                            mResources.getString(R.string.last_online_timestamp_default)));
                }
            }

        };
        mContactsList.setAdapter(mContactsAdapter);
    }
}
