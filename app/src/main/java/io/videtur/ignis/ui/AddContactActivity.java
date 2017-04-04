package io.videtur.ignis.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.videtur.ignis.R;
import io.videtur.ignis.model.User;
import io.videtur.ignis.core.IgnisAuthActivity;
import io.videtur.ignis.util.Util;

import static io.videtur.ignis.util.Constants.CONTACTS_REF;
import static io.videtur.ignis.util.Constants.USERS_REF;

public class AddContactActivity extends IgnisAuthActivity {

    private static final String TAG = "AddContactActivity";

    private TextInputLayout mTextInputLayout;
    private EditText mEditText;

    private DatabaseReference mUserContactsRef;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout);
        mEditText = (EditText) findViewById(R.id.edit_text);

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addContact();
                    handled = true;
                }
                return handled;
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onUserDataChange(String key, User user) {
        super.onUserDataChange(key, user);
        mUser = user;
        mUserContactsRef = getDatabase().getReference(CONTACTS_REF).child(key);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                addContact();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addContact() {
        String contactEmail = mEditText.getText().toString();
        if (contactEmail.isEmpty()) {
            showContactError(R.string.error_no_email);
        } else if (contactEmail.equals(mUser.getEmail())) {
            showContactError(R.string.error_own_email);
        } else {
            String contactKey = Util.getKeyFromEmail(contactEmail);
            DatabaseReference contactUserRef = getDatabase().getReference(USERS_REF).child(contactKey);
            contactUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        addUserToContacts(dataSnapshot.getValue(User.class));
                    } else {
                        showContactError(R.string.error_invalid_email);
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
    }

    private void addUserToContacts(final User contact) {
        String contactKey = Util.getKeyFromEmail(contact.getEmail());
        final DatabaseReference contactRef = mUserContactsRef.child(contactKey);
        contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check contact doesn't already exist
                if (!dataSnapshot.exists()) {
                    contactRef.setValue(contact.getName().toLowerCase(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.e(TAG, databaseError.getMessage());
                            } else {
                                showToast(R.string.contact_added);
                                startContactsActivity();
                            }
                        }
                    });
                } else {
                    showContactError(R.string.contact_already_added);
                    startContactsActivity();
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

    private void startContactsActivity() {
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    private void showContactError(int error) {
        mTextInputLayout.setError(getResources().getString(error));
    }
}
