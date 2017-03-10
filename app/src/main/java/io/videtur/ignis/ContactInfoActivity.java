package io.videtur.ignis;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;

import static io.videtur.ignis.util.Constants.CONTACTS_REF;
import static io.videtur.ignis.util.Constants.USERS_REF;
import static io.videtur.ignis.util.Util.dpToPx;
import static io.videtur.ignis.util.Util.formatLastOnlineTime;

public class ContactInfoActivity extends IgnisAuthActivity {

    private static final String TAG = "ContactInfoActivity";

    public static final String ARG_CONTACT_KEY = "arg_contact_key";

    private ImageView mContactAvatar;
    private TextView mContactName;
    private TextView mContactStatus;
    private TextView mContactEmail;
    private View mEmailView;
    private Button mAddContactButton;
    private Button mDeleteContactButton;

    private DatabaseReference mUserContactsRef;
    private ContactExistsListener mContactExistsListener;
    private DatabaseReference mContactRef;
    private ContactListener mContactListener;
    private String mContactKey;
    private String mUserKey;
    private String mEmail;
    private String mName;
    private UserIconFactory mUserIconFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUserIconFactory = new UserIconFactory(this);

        mContactAvatar = (ImageView) findViewById(R.id.cat_avatar);
        mContactName = (TextView) findViewById(R.id.cat_title);
        mContactStatus = (TextView) findViewById(R.id.subtitle);
        mContactEmail = (TextView) findViewById(R.id.contact_email);

        mEmailView = findViewById(R.id.email_view);
        registerForContextMenu(mEmailView);
        mEmailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmailActivity();
            }
        });

        mAddContactButton = (Button) findViewById(R.id.add_contact);
        mDeleteContactButton = (Button) findViewById(R.id.delete_contact);

        mAddContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserContactsRef.setValue(mName.toLowerCase(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.e(TAG, databaseError.getMessage());
                        } else {
                            showToast(R.string.contact_added);
                        }
                    }
                });
            }
        });

        mDeleteContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserContactsRef.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.e(TAG, databaseError.getMessage());
                        } else {
                            showToast(R.string.contact_deleted);
                        }
                    }
                });
            }
        });

        mContactKey = getIntent().getStringExtra(ARG_CONTACT_KEY);
        mContactRef = getDatabase().getReference(USERS_REF).child(mContactKey);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO start ChatActivity with contact
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContactListener = (ContactListener)
                mContactRef.addValueEventListener(new ContactListener());
        if (mUserContactsRef != null) {
            mContactExistsListener = (ContactExistsListener)
                    mUserContactsRef.addValueEventListener(new ContactExistsListener());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContactRef.removeEventListener(mContactListener);
        removeContactExistsListener();
    }

    @Override
    public void onUserDataChange(String key, User user) {
        super.onUserDataChange(key, user);
        mUserKey = key;
        removeContactExistsListener();
        mUserContactsRef = getDatabase().getReference(CONTACTS_REF).child(mUserKey).child(mContactKey);
        mContactExistsListener = (ContactExistsListener)
                mUserContactsRef.addValueEventListener(new ContactExistsListener());
    }

    private void removeContactExistsListener() {
        if (mContactExistsListener != null) {
            mUserContactsRef.removeEventListener(mContactExistsListener);
            mContactExistsListener = null;
        }
    }

    private class ContactListener implements ValueEventListener {
        private static final String TAG = "ContactListener";

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                final User contact = dataSnapshot.getValue(User.class);
                int iconSize = (int) dpToPx(ContactInfoActivity.this, 40);

                mName = contact.getName();
                mEmail = contact.getEmail();
                Drawable userIconPlaceholder = mUserIconFactory.getDefaultAvatar(contact.getName(),
                        contact.getEmail(), iconSize, iconSize);
                Glide.with(ContactInfoActivity.this)
                        .load(contact.getPhotoUrl())
                        .placeholder(userIconPlaceholder)
                        .crossFade()
                        .into(mContactAvatar);
                mContactName.setText(mName);
                mContactEmail.setText(mEmail);
                if (contact.getConnections() != null && contact.getConnections().size() > 0) {
                    mContactStatus.setText(R.string.online);
                } else {
                    mContactStatus.setText(formatLastOnlineTime(contact.getLastOnline()));
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, databaseError.getMessage());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.context_menu_contact_info, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_send_email:
                startEmailActivity();
                return true;
            case R.id.context_copy_email:
                copyEmail();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void startEmailActivity() {
        if (mEmail != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mEmail});
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }

    private void copyEmail() {
        if (mEmail != null) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("email", mEmail);
            clipboardManager.setPrimaryClip(clipData);
            showToast(R.string.copied_to_clipboard);
        }
    }

    private class ContactExistsListener implements ValueEventListener {
        private static final String TAG = "ContactExistsListener";

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                mAddContactButton.setVisibility(View.GONE);
                mDeleteContactButton.setVisibility(View.VISIBLE);
            } else {
                mAddContactButton.setVisibility(View.VISIBLE);
                mDeleteContactButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, databaseError.getMessage());
        }
    }
}