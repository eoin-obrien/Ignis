package io.videtur.ignis;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

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
    private DatabaseReference mContactsKeyRef;
    private DatabaseReference mUsersRef;
    private FirebaseIndexListAdapter<User> mContactsAdapter;
    private TextWatcher mSearchTextWatcher;

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

        mContactsList.setEmptyView(findViewById(R.id.empty_search));

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_contact:
                // TODO start NewContactActivity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onUserDataChange(final String key, User user) {
        super.onUserDataChange(key, user);

        mContactsKeyRef = mContactsRef.child(key);
        setContactsAdapter();
        mContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String contactKey = mContactsAdapter.getRef(position).getKey();
                // TODO start ContactInfoActivity or ChatActivity for selected contact
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
                Glide.with(ContactsActivity.this).load(model.getPhotoUrl()).fitCenter().into(contactPhoto);
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
