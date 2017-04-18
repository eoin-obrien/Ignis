package io.videtur.ignis.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.common.SignInButton;

import java.util.Collections;
import java.util.List;

import io.videtur.ignis.R;

import static io.videtur.ignis.core.Constants.REQUEST_SIGN_IN;

/**
 * Provides a simple interface for Google authentication through Firebase.
 */
public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SignInButton googleSignInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGoogleSignIn();
            }
        });
    }

    private void startGoogleSignIn() {
        // Build and start FirebaseUI authentication flow
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                REQUEST_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Only handle results from FirebaseUI authentication
        if (requestCode == REQUEST_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == ResultCodes.OK) {
                // Authentication successful, launch MainActivity
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                // Authentication failed, show error in toast
                if (response != null) {
                    Log.d(TAG, "errorCode:" + response.getErrorCode());
                    if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                        showToast(R.string.sign_in_no_network);
                    } else {
                        showToast(R.string.sign_in_unknown_error);
                    }
                }
            }

        }
    }

    private void showToast(int stringResource) {
        Toast.makeText(this, stringResource, Toast.LENGTH_SHORT).show();
    }

}
