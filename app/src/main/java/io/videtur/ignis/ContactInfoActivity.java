package io.videtur.ignis;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;

import static io.videtur.ignis.util.Constants.USERS_REF;
import static io.videtur.ignis.util.Util.formatLastOnlineTime;

public class ContactInfoActivity extends IgnisAuthActivity {

    private static final String TAG = "ContactInfoActivity";

    public static final String ARG_CONTACT_KEY = "arg_contact_key";

    private ImageView mContactAvatar;
    private TextView mContactName;
    private TextView mContactStatus;

    private DatabaseReference mContactRef;
    private ContactListener mContactListener;
    private String mContactKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContactAvatar = (ImageView) findViewById(R.id.cat_avatar);
        mContactName = (TextView) findViewById(R.id.cat_title);
        mContactStatus = (TextView) findViewById(R.id.subtitle);

        mContactKey = getIntent().getStringExtra(ARG_CONTACT_KEY);
        mContactRef = getDatabase().getReference(USERS_REF).child(mContactKey);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        mContactListener = (ContactListener) mContactRef.addValueEventListener(new ContactListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContactRef.removeEventListener(mContactListener);
    }

    private class ContactListener implements ValueEventListener {
        private static final String TAG = "ContactListener";

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                User contact = dataSnapshot.getValue(User.class);
                Glide.with(ContactInfoActivity.this).load(contact.getPhotoUrl()).into(mContactAvatar);
                mContactName.setText(contact.getName());
                mContactStatus.setText(formatLastOnlineTime(contact.getLastOnline()));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
