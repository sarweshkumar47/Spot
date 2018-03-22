package com.makesense.labs.spot.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.makesense.labs.spot.R;
import com.makesense.labs.spot.main.MainActivity;
import com.makesense.labs.spot.utils.ConnectivityUtils;
import com.makesense.labs.spot.utils.Constants;
import com.makesense.labs.spot.utils.SharedPrefsUtils;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";

    @BindView(R.id.rootCoordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;

    private Snackbar mSnackBar;

    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Checks whether user has already logged in or not
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "SpotApp, LoginActivity, user already logged in");
            callMainActivity();
        }
    }

    /*
     *  Shows firebase prebuilt sign-ui
     */
    private void showSignInScreen() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.FacebookBuilder().build(),
                                new AuthUI.IdpConfig.TwitterBuilder().build()))
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.LoginTheme)
                        .build(),
                Constants.FIRE_BASE_SIGN_IN);
    }

    /*
     *  Starts MainActivity() after user verification
     */
    private void callMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    /*
     * Result returned from launching the intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "SpotApp, LoginActivity, onActivityResult, " +
                "requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == Constants.FIRE_BASE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                writeToSharedPreferences(user);
                callMainActivity();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign-in failed
                Log.d(TAG, "SpotApp, LoginActivity, onActivityResult, back press");
                Toast.makeText(getBaseContext(), R.string.sign_in_error, Toast.LENGTH_SHORT).show();
                checkConnection();
            }
        }
    }

    /**
     * Writes user details into shared preferences
     *
     * @param firebaseUser Object consists of user details after successful sign in
     */
    private void writeToSharedPreferences(FirebaseUser firebaseUser) {
        Log.d(TAG, "SpotApp, LoginActivity, writeToSharedPreferences: " +
                firebaseUser.getProviderId());
        // Store email id, name and photo url in shared preferences
        SharedPrefsUtils.setStringPreference(getApplicationContext(), getString(R.string.user_name),
                firebaseUser.getDisplayName());
        SharedPrefsUtils.setStringPreference(getApplicationContext(), getString(R.string.user_email_id),
                firebaseUser.getEmail());
        if (firebaseUser.getPhotoUrl() != null) {
            SharedPrefsUtils.setStringPreference(getApplicationContext(), getString(R.string.user_photo_url),
                    firebaseUser.getPhotoUrl().toString());
        }
    }

    // Manually checks connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityUtils.isConnected(getApplicationContext());
        Log.d(TAG, "SpotApp, LoginActivity, ConnectivityChangeReceiver: " + isConnected);
        if (isConnected) {
            displaySnackBar(true, getString(R.string.info_internet_connection));
        } else {
            displaySnackBar(false, getString(R.string.warning_no_connection));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "SpotApp, LoginActivity, onStart()");
        if (connectivityChangeReceiver == null) {
            connectivityChangeReceiver = new ConnectivityChangeReceiver();
        }
        registerReceiver(connectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "SpotApp, LoginActivity, onStop()");
        unregisterReceiver(connectivityChangeReceiver);
        if (connectivityChangeReceiver != null) {
            connectivityChangeReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
     * Display connection information in snack bar
     */
    private void displaySnackBar(boolean connection, String message) {
        mSnackBar = Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
        mSnackBar.setAction(R.string.snack_bar_sign_in, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignInScreen();
            }
        });
        mSnackBar.getView().getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mSnackBar.getView().getViewTreeObserver().removeOnPreDrawListener(this);
                        ((CoordinatorLayout.LayoutParams) mSnackBar.getView().getLayoutParams())
                                .setBehavior(null);
                        return true;
                    }
                });
        if (connection) {
            mSnackBar.setActionTextColor(ContextCompat.getColor(this, R.color.snack_bar_green_color));
        } else {
            mSnackBar.setActionTextColor(ContextCompat.getColor(this, R.color.snack_bar_red_color));
        }
        mSnackBar.show();
    }

    private class ConnectivityChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {
            checkConnection();
        }
    }
}
