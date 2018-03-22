package com.makesense.labs.spot.main;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.makesense.labs.spot.R;
import com.makesense.labs.spot.about.AboutPageActivity;
import com.makesense.labs.spot.license.SoftwareLicenseActivity;
import com.makesense.labs.spot.login.LoginActivity;
import com.makesense.labs.spot.services.GPSService;
import com.makesense.labs.spot.utils.Constants;
import com.makesense.labs.spot.utils.FileUtils;
import com.makesense.labs.spot.utils.PermissionUtils;
import com.makesense.labs.spot.utils.SharedPrefsUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class MainActivity extends AppCompatActivity implements
        OnCompleteListener<LocationSettingsResponse>,
        AppLogOutListener,
        ToolbarUiUpdateListener,
        UploadFilesActionListener {

    private String TAG = "MainActivity";

    @BindView(R.id.rootCoordinatorLayout)
    CoordinatorLayout mRootCoordinatorLayout;

    @BindView(R.id.appBarLayout)
    AppBarLayout mAppbarLayout;

    @BindView(R.id.collapsingToolbarLayout)
    SubtitleCollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.profilePicImageView)
    ImageView mProfilePicImageView;

    @BindView(R.id.userNameTextView)
    TextView mUserNameTextView;

    @BindView(R.id.userEmailTextView)
    TextView mUserEmailTextView;

    private Menu menu;
    private Snackbar mPermissionSnackBar;
    private Toast toastMessage;

    private MapsFragment mapsFragment;

    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    private Messenger mGPSService;

    private boolean isAppBarExpanded = false;
    private boolean isGPSServiceBound = false;
    private boolean mLocationSettingsRequestShowing = false;
    private boolean isLocationEnabled = false;
    private boolean showUploadFilesMenuItem = false;

    private final String[] appPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mapsFragment = MapsFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                mapsFragment).commit();

        // Checks for location and camera permissions
        if (!(PermissionUtils.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
                && PermissionUtils.hasPermissions(this, Manifest.permission.CAMERA))) {
            requestAppPermission();
        }


        // Appbar change listener
        mAppbarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    // Appbar collapsed
                    updateAppBarViewLayout(false);
                } else if (verticalOffset == 0) {
                    // Appbar expanded
                    updateAppBarViewLayout(true);
                } else {
                    // Somewhere in between
                }
            }
        });

        // sets username and user email id in appbar layout
        String userName = SharedPrefsUtils.getStringPreference(getApplicationContext(),
                getString(R.string.user_name));
        String userEmail = SharedPrefsUtils.getStringPreference(getApplicationContext(),
                getString(R.string.user_email_id));
        if (userName != null) {
            mUserNameTextView.setText(userName);
        }
        if (userEmail != null) {
            mUserEmailTextView.setText(userEmail);
        } else {
            mUserEmailTextView.setText(R.string.dash_symbol);
        }

        String userPicUrl = SharedPrefsUtils.getStringPreference(getApplicationContext(),
                getString(R.string.user_photo_url));
        if (userPicUrl != null) {
            // Loads user's profile pic from an url or a default image
            Glide.with(this)
                    .load(userPicUrl)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.ic_person_white_48dp))
                    .into(mProfilePicImageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_items, menu);
        this.menu = menu;
        final MenuItem uploadMenuItem = menu.findItem(R.id.cloud_upload);
        View uploadActionView = uploadMenuItem.getActionView();
        uploadActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(uploadMenuItem);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "SpotApp, MainActivity onOptionsItemSelected: " + item.getItemId());

        switch (item.getItemId()) {

            case R.id.cloud_upload:
                Log.d(TAG, "SpotApp, MainActivity onOptionsItemSelected, cloud_upload()");
                showUploadingFilesPauseAlert();
                break;

            case R.id.location_search:
                launchPlacesAutoCompleteView();
                break;

            case R.id.user_profile:
                if (mAppbarLayout.getTop() < 0) {
                    expandOrCollapseAppBarLayout(true);
                } else {
                    expandOrCollapseAppBarLayout(false);
                }
                break;

            case R.id.license:
                Intent licensePageIntent = new Intent(MainActivity.this, SoftwareLicenseActivity.class);
                startActivity(licensePageIntent);
                break;

            case R.id.about:
                Intent aboutPageIntent = new Intent(MainActivity.this, AboutPageActivity.class);
                startActivity(aboutPageIntent);
                break;

            case R.id.user_logout:
                showLogOutAlert();
                break;

            // Home up button
            case android.R.id.home:
                expandOrCollapseAppBarLayout(false);
                return true;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "SpotApp, MainActivity onStart()");
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!mLocationSettingsRequestShowing) {
                createLocationRequest();
                buildLocationSettingsRequest();
                checkLocationSettings();
            }
        }
        // Starts bind service
        Intent intent = new Intent(this, GPSService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "SpotApp, MainActivity onStop()");
        try {
            if (isGPSServiceBound) {
                unbindService(mServiceConnection);
                isGPSServiceBound = false;
            }
        } catch (Exception ignore) {
        }
        if (toastMessage != null) {
            toastMessage.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "SpotApp, MainActivity onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isAppBarExpanded) {
            mAppbarLayout.setExpanded(false, true);
            return;
        }
        super.onBackPressed();
    }

    protected void createLocationRequest() {
        Log.i(TAG, "SpotApp, MainActivity createLocationRequest");
        mLocationRequest = LocationRequest.create();
        /*
         * Sets the desired interval for active location updates. This interval is
         * inexact. You may not receive updates at all if no location sources are available, or
         * you may receive them slower than requested. You may also receive updates faster than
         * requested if other applications are requesting location at a faster interval.
         */
        mLocationRequest.setInterval(Constants.LOCATION_INTERVAL);
        /* Sets the fastest rate for active location updates. This interval is exact, and your
         * application will never receive updates faster than this value.
         */
        mLocationRequest.setFastestInterval(Constants.LOCATION_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        Log.i(TAG, "SpotApp, MainActivity buildLocationSettingsRequest");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        Log.i(TAG, "SpotApp, MainActivity checkLocationSettings");
        mLocationSettingsRequestShowing = true;
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(mLocationSettingsRequest);
        result.addOnCompleteListener(this);
    }

    @Override
    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
        try {
            LocationSettingsResponse response = task.getResult(ApiException.class);
            Log.d(TAG, "SpotApp, MainActivity onResult SUCCESS");
            // All location settings are satisfied
            isLocationEnabled = true;
            mLocationSettingsRequestShowing = false;
            if (isGPSServiceBound) {
                mRequestService(Constants.LOCATION_START);
            }
        } catch (ApiException exception) {
            switch (exception.getStatusCode()) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.d(TAG, "SpotApp, MainActivity onResult RESOLUTION_REQUIRED");
                    try {
                        isLocationEnabled = false;
                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                        // Shows the dialog and check the result in onActivityResult()
                        resolvable.startResolutionForResult(MainActivity.this,
                                Constants.LOCATION_ENABLE_INTENT_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException | ClassCastException ignore) {
                    }
                    break;

                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    isLocationEnabled = false;
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    Log.d(TAG, "SpotApp, MainActivity onResult SETTINGS_CHANGE_UNAVAILABLE");
                    break;
            }
        }
    }

    /*
     * Result returned from launching the intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "SpotApp, MainActivity onActivityResult, " + requestCode + ", " + resultCode);
        switch (requestCode) {
            case Constants.LOCATION_ENABLE_INTENT_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "SpotApp, MainActivity onActivityResult RESULT_OK");
                        isLocationEnabled = true;
                        mLocationSettingsRequestShowing = false;
                        if (isGPSServiceBound) {
                            mRequestService(Constants.LOCATION_START);
                        }
                        break;

                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        MainActivity.this.finish();
                        break;
                    }
                }
                break;

            case Constants.APP_PERMISSIONS_CHANGE_REQUEST_CODE:
                if (!(PermissionUtils.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        && PermissionUtils.hasPermissions(this, Manifest.permission.CAMERA))) {
                    requestAppPermission();
                }
                break;

            case Constants.PLACES_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    mapsFragment.moveMapCameraToSearchLocation(
                            PlaceAutocomplete.getPlace(this, data));
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.e(TAG, "SpotApp, MainActivity, places autocomplete " + status.getStatusMessage());
                    mapsFragment.moveMapCameraToUserLocation();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.e(TAG, "SpotApp, MainActivity ACTIVITY RESULT CANCELED");
                    mapsFragment.moveMapCameraToUserLocation();
                }
                break;

            case Constants.CAMERA_CAPTURE_INTENT_REQUEST_CODE:
                Log.e(TAG, "SpotApp, MainActivity CAMERA_CAPTURE_INTENT_REQUEST_CODE");
                if (resultCode == Activity.RESULT_CANCELED) {
                    Log.e(TAG, "SpotApp, MainActivity CAMERA_CAPTURE cancelled");
                    mapsFragment.cameraCaptureCancel();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /*
     * Service connection
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "SpotApp, MainActivity onServiceConnected()");
            mGPSService = new Messenger(service);
            isGPSServiceBound = true;
            // If location is enabled, requests for location updates
            if (isLocationEnabled) {
                mRequestService(Constants.LOCATION_START);
            }
        }

        /*
         * Service disconnected
         */
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "SpotApp, MainActivity onServiceDisconnected()");
            isGPSServiceBound = false;
        }
    };

    /*
     * Creates a message and sends it to GPS Service
     * @param mRequestCode - Constants
     */
    private void mRequestService(int mRequestCode) {
        Log.d(TAG, "SpotApp, MainActivity mRequestService()");
        Message msg = Message.obtain(null, mRequestCode);
        try {
            mGPSService.send(msg);
        } catch (RemoteException e) {
            Log.d(TAG, "SpotApp, MainActivity mRequestService() RemoteException");
        }
    }

    /*
     *  Expands or collapses the appbar layout
     * @param expanded - true if the layout should be fully expanded, false if it should
     *          be fully collapsed
     */
    private void expandOrCollapseAppBarLayout(boolean expanded) {
        mAppbarLayout.setExpanded(expanded, true);
    }

    /**
     * Updates appbar layout after expanding or collapsing
     *
     * @param expanded true if appbar is fully expanded, false otherwise
     */
    private void updateAppBarViewLayout(boolean expanded) {
        if (expanded) {
            mapsFragment.coverBlackOverLay();
            isAppBarExpanded = true;
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            // Hides menu items
            if (menu != null) {
                menu.findItem(R.id.location_search).setVisible(false);
                menu.findItem(R.id.user_profile).setVisible(false);
                menu.findItem(R.id.cloud_upload).setVisible(false);
                menu.findItem(R.id.license).setVisible(false);
                menu.findItem(R.id.about).setVisible(false);
                menu.findItem(R.id.user_logout).setVisible(false);
            }
            // Shows home up indicator
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar()
                        .setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            }
            mCollapsingToolbarLayout.setTitle(getString(R.string.title_profile));
        } else {
            mapsFragment.removeBlackOverLay();
            isAppBarExpanded = false;
            // Hides home up indicator
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            mCollapsingToolbarLayout.setTitle(getString(R.string.app_name));
            // Shows menu items
            if (menu != null) {
                menu.findItem(R.id.location_search).setVisible(true);
                menu.findItem(R.id.user_profile).setVisible(true);
                menu.findItem(R.id.cloud_upload).setVisible(showUploadFilesMenuItem);
                menu.findItem(R.id.license).setVisible(true);
                menu.findItem(R.id.about).setVisible(true);
                menu.findItem(R.id.user_logout).setVisible(true);
            }
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onAppSignOutConfirm(boolean accountDelete) {
        if (accountDelete) {
            firebaseAccountDelete();
        } else {
            appLogOut();
        }
    }

    /*
     * Disconnects the user's account from the app.
     * If it succeeds, clear the information and launches app login screen or shows an error message
     */
    private void appLogOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "SpotApp, MainActivity, logout success()");
                        if (mapsFragment != null) {
                            mapsFragment.discardAllUserData();
                        }
                        FileUtils.deleteAllFiles(getApplicationContext());
                        startLogOutProcess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (toastMessage != null) {
                            toastMessage.cancel();
                        }
                        toastMessage = Toast.makeText(getBaseContext(),
                                R.string.log_out_error, Toast.LENGTH_SHORT);
                        toastMessage.show();
                    }
                });
    }

    /*
     * Shows an alert dialog, when the user clicks on SIGN OUT button
     */
    private void showLogOutAlert() {
        Log.d(TAG, "SpotApp, MainActivity, showLogOutAlert()");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("logout");
        if (prev != null) {
            ft.remove(prev);
        }
        LogOutAlertDialogFragment logOutAlertDialogFragment = LogOutAlertDialogFragment.newInstance();
        logOutAlertDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        logOutAlertDialogFragment.setCancelable(true);
        logOutAlertDialogFragment.show(ft, "logout");
    }

    private void firebaseAccountDelete() {
        AuthUI.getInstance()
                .delete(this)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "SpotApp, MainActivity, ac delete success()");
                        if (mapsFragment != null) {
                            mapsFragment.discardAllUserData();
                        }
                        FileUtils.deleteAllFiles(getApplicationContext());
                        startLogOutProcess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (toastMessage != null) {
                            toastMessage.cancel();
                        }
                        toastMessage = Toast.makeText(getBaseContext(),
                                R.string.log_out_error, Toast.LENGTH_SHORT);
                        toastMessage.show();
                    }
                });
    }

    /*
     * Clears shared preferences and launches app login screen
     */
    private void startLogOutProcess() {
        Log.d(TAG, "SpotApp, MainActivity, startSignOutProcess");

        // Clears shared preferences
        SharedPrefsUtils.clearPreferences(this);

        // Intent to launch app tutorial page
        Intent logOutIntent = new Intent(this, LoginActivity.class);
        logOutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        logOutIntent.setAction(Intent.ACTION_MAIN);
        logOutIntent.addCategory(Intent.CATEGORY_HOME);
        logOutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        logOutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logOutIntent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void launchPlacesAutoCompleteView() {
        try {
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            overridePendingTransition(0, 0);
            startActivityForResult(intent, Constants.PLACES_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException ge) {
            Log.e(TAG, "SpotApp, MainActivity, launchPlacesAutoCompleteView GooglePlayServicesRepairableException");
        } catch (GooglePlayServicesNotAvailableException en) {
            Log.e(TAG, "SpotApp, MainActivity, launchPlacesAutoCompleteView GooglePlayServicesNotAvailableException");
        }
    }

    /*
     * Shows an alert dialog, when the user clicks on SIGN OUT button
     */
    private void showUploadingFilesPauseAlert() {
        Log.d(TAG, "SpotApp, MainActivity, showUploadingFilesPauseAlert()");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("pause");
        if (prev != null) {
            ft.remove(prev);
        }
        UploadFilesAlertDialogFragment uploadFilesAlertDialogFragment = UploadFilesAlertDialogFragment.newInstance();
        uploadFilesAlertDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        uploadFilesAlertDialogFragment.setCancelable(true);
        uploadFilesAlertDialogFragment.show(ft, "pause");
    }

    @Override
    public void updateToolbarSubtitle(String subtitle) {
        mCollapsingToolbarLayout.setSubtitle(subtitle);
    }

    @Override
    public void showUploadFileMenuItem(boolean show) {
        if (menu != null)
            menu.findItem(R.id.cloud_upload).setVisible(show);
        showUploadFilesMenuItem = show;
    }

    @Override
    public void pauseAllPendingTasks() {
        showUploadFileMenuItem(false);
        if (mapsFragment != null) {
            mapsFragment.pauseAllPendingTasks();
        }
    }

    /*
     * Requests location and camera permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it requests permissions directly.
     */
    private void requestAppPermission() {
        Log.d(TAG, "SpotApp, MainActivity requestLocationPermission()");

        if (PermissionUtils.showRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ||
                PermissionUtils.showRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
            prepareSnackBarForPermissions("Allow location and camera permissions for the app",
                    "GRANT");
        } else {
            Log.d(TAG, "SpotApp, MainActivity requestPermissions()");
            PermissionUtils.requestPermissions(MainActivity.this, appPermissions,
                    Constants.APP_PERMISSIONS_ALLOW_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "SpotApp, MainActivity onRequestPermissionsResult()");

        if (requestCode == Constants.APP_PERMISSIONS_ALLOW_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                Log.d(TAG, "SpotApp, MainActivity onRequestPermissionsResult() permission missing");
                if (PermissionUtils.showRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) ||
                        PermissionUtils.showRequestPermissionRationale(this,
                                Manifest.permission.CAMERA)) {
                    prepareSnackBarForPermissions("Allow location and camera permissions for the app",
                            "GRANT");
                } else {
                    prepareSnackBarForPermissions("Please enable location and camera permissions from settings",
                            "ENABLE");
                }
            } else {
                Log.d(TAG, "SpotApp, MainActivity onRequestPermissionsResult() got all permissions");
                if (!mLocationSettingsRequestShowing) {
                    createLocationRequest();
                    buildLocationSettingsRequest();
                    checkLocationSettings();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void prepareSnackBarForPermissions(String title, String action) {
        mPermissionSnackBar = Snackbar.make(mRootCoordinatorLayout, title,
                Snackbar.LENGTH_INDEFINITE);
        mPermissionSnackBar.getView().getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mPermissionSnackBar.getView().getViewTreeObserver()
                                .removeOnPreDrawListener(this);
                        ((CoordinatorLayout.LayoutParams) mPermissionSnackBar
                                .getView().getLayoutParams()).setBehavior(null);
                        return true;
                    }
                });
        if (action.equalsIgnoreCase("GRANT")) {
            mPermissionSnackBar.setAction(R.string.snack_bar_grant, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PermissionUtils.requestPermissions(MainActivity.this, appPermissions,
                            Constants.APP_PERMISSIONS_ALLOW_REQUEST_CODE);
                }
            });
            mPermissionSnackBar.setActionTextColor(ContextCompat.getColor(this,
                    R.color.snack_bar_green_color));
        } else {
            mPermissionSnackBar.setAction(R.string.snack_bar_enable, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent()
                            .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .addCategory(Intent.CATEGORY_DEFAULT)
                            .setData(Uri.parse("package:" + getPackageName()))
                            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivityForResult(intent,
                            Constants.APP_PERMISSIONS_CHANGE_REQUEST_CODE);
                }
            });

            mPermissionSnackBar.setActionTextColor(ContextCompat.getColor(this,
                    R.color.snack_bar_red_color));
        }
        mPermissionSnackBar.show();
    }
}
