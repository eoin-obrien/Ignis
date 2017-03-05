package io.videtur.ignis;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseIndexListAdapter;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import io.videtur.ignis.model.User;
import io.videtur.ignis.util.IgnisAuthActivity;

import static io.videtur.ignis.util.Constants.CONTACTS_REF;
import static io.videtur.ignis.util.Constants.USERS_REF;

public class ContactsActivity extends IgnisAuthActivity {

    private static final String TAG = "ContactsActivity";

    private DatabaseReference mContactsRef;
    private DatabaseReference mUsersRef;

    private EditText mSearchEditText;
    private ListView mContactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSearchEditText = (EditText) findViewById(R.id.contacts_search_edit_text);
        mContactsList = (ListView) findViewById(R.id.contacts_list);

        // Setup database references
        mContactsRef = getDatabase().getReference(CONTACTS_REF);
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
    public void onUserDataChange(String key, User user) {
        super.onUserDataChange(key, user);

        mContactsList.setAdapter(new FirebaseIndexListAdapter<User>(this, User.class, R.layout.list_item_contact, mContactsRef.child(key), mUsersRef) {
            @Override
            protected void populateView(View v, User model, int position) {
                ImageView contactPhoto = (ImageView) v.findViewById(R.id.contact_profile_image);
                TextView contactName = (TextView) v.findViewById(R.id.contact_user_name);
                TextView contactStatus = (TextView) v.findViewById(R.id.contact_user_status);
                Glide.with(ContactsActivity.this).load(model.getPhotoUrl()).fitCenter().into(contactPhoto);
                contactName.setText(model.getName());
                if (model.getConnections() != null && model.getConnections().size() > 0) {
                    contactStatus.setText("online");
                } else {
                    contactStatus.setText(formatLastOnlineTime(model.getLastOnline()));
                }
            }
        });
    }

    private String formatLastOnlineTime(long lastOnlineMilliseconds) {
        String formattedLastOnlineTime;
        Calendar now = Calendar.getInstance();
        Calendar lastOnline = Calendar.getInstance();
        lastOnline.setTimeInMillis(lastOnlineMilliseconds);

        long difference = now.getTimeInMillis() - lastOnline.getTimeInMillis();
        long daysDifference = TimeUnit.MILLISECONDS.toDays(difference);
        boolean sameYear = now.get(Calendar.YEAR) == lastOnline.get(Calendar.YEAR);
        boolean sameDay = sameYear && now.get(Calendar.DAY_OF_YEAR) == lastOnline.get(Calendar.DAY_OF_YEAR);
        boolean sameWeek = daysDifference < 7;

        if (sameDay) {
            SimpleDateFormat formatter = new SimpleDateFormat("'last seen at' HH:mm");
            formattedLastOnlineTime = formatter.format(lastOnline.getTime());
        } else if (sameWeek) {
            SimpleDateFormat formatter = new SimpleDateFormat("'last seen' EEE 'at' HH:mm");
            formattedLastOnlineTime = formatter.format(lastOnline.getTime());
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("'last seen' dd.MM.yy 'at' HH:mm");
            formattedLastOnlineTime = formatter.format(lastOnline.getTime());
        }
        return formattedLastOnlineTime;
    }
}
