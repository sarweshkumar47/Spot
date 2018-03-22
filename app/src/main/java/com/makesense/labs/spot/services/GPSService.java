package com.makesense.labs.spot.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.makesense.labs.spot.utils.Constants;

import java.lang.ref.WeakReference;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class GPSService extends Service {

    private String TAG = "GPSService";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    private Messenger mMessenger;

    /**
     * Handler of incoming messages from clients (activity).
     */
    private static class mServiceHandler extends Handler {
        private final WeakReference<GPSService> myClassWeakReference;
        private GPSService mService;

        mServiceHandler(GPSService myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
            this.mService = myClassWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(mService.TAG, "SpotApp, GPSService mServiceHandler");
            switch (msg.what) {
                case Constants.LOCATION_START:
                    Log.d(mService.TAG, "SpotApp, GPSService mServiceHandler LOCATION_START");
                    mService.startLocationUpdates();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    protected void createLocationRequest() {
        Log.i(TAG, "SpotApp, GPSService createLocationRequest");
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

    final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                Intent intent = new Intent(Constants.LOCATION_BROADCAST_KEY);
                sendLocationBroadcast(intent, location);
            }
        }
    };

    protected void startLocationUpdates() {
        createLocationRequest();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "SpotApp, GPSService, no permission");
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Intent intent = new Intent(Constants.LOCATION_BROADCAST_KEY);
                            sendLocationBroadcast(intent, location);
                        }
                    }
                });

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void sendLocationBroadcast(Intent intent, Location loc) {
        intent.putExtra("latitude", loc.getLatitude());
        intent.putExtra("longitude", loc.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SpotApp, GPSService onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "SpotApp, GPSService onBind()");
        //mMessenger = new Messenger(new mActivityHandler(this));
        mMessenger = new Messenger(new mServiceHandler(this));
        return mMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        // Is called when activity issues a `bindService` after an `undbindService`
        super.onRebind(intent);
        Log.d(TAG, "SpotApp, GPSService onRebind()");
        mMessenger = new Messenger(new mServiceHandler(this));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "SpotApp, GPSService onUnbind()");
        stopLocationUpdates();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SpotApp, GPSService onDestroy()");
    }
}