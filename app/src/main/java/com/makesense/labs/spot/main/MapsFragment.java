package com.makesense.labs.spot.main;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.makesense.labs.spot.R;
import com.makesense.labs.spot.model.SpotData;
import com.makesense.labs.spot.utils.Constants;
import com.makesense.labs.spot.utils.FileUtils;
import com.makesense.labs.spot.utils.SharedPrefsUtils;
import com.makesense.labs.spot.utils.StringUtils;
import com.makesense.labs.spot.utils.ViewUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback,
        FirebaseDataPushListener {

    public static MapsFragment newInstance() {
        return new MapsFragment();
    }

    private String TAG = "MapsFragment";

    @BindView(R.id.firebaseConnectionTextView)
    TextView mFirebaseConnectivityView;

    private TextView mLastReportView;
    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFragment;

    @BindView(R.id.tapFloatingActionButton)
    FloatingActionButton tapFloatingActionButton;

    @BindView(R.id.captureFloatingActionButton)
    FloatingActionButton captureFloatingActionButton;

    @BindView(R.id.myLocationButton)
    Button myLocationButton;

    @BindView(R.id.transparentView)
    View transparentView;

    private ViewGroup mViewGroup;
    private View rootView;

    private ImageView mPotHoleImageView;
    private ImageView mAccidentImageView;
    private TextView mPotHoleTextView;
    private TextView mAccidentTextView;

    private SimpleDateFormat mSimpleDateFormatForCloudDataPush;

    private Activity activity;

    private StorageReference storageReference;
    private DatabaseReference connectionReference;
    private DatabaseReference userDataReference;

    private String reportingType = Constants.POTHOLE_TYPE;
    private boolean isMapCameraMovedToMyLocation = false;
    private double mCurrentLatitude = 0, mCurrentLongitude = 0;
    private float mDefaultZoomInLevel = 16;
    private String mUserEmailId;
    private Gson gson;
    private FirebaseDataPost firebaseDataPost;
    private Marker mapMarker;
    private boolean addDataPointByDraggingOrSearch = false;
    private boolean discardUserData = false;
    private Uri mImageCaptureUri;
    private LinkedHashMap<String, SpotData> spotDataImageCaptureHashMap;
    private Toast toastMessage;
    private ToolbarUiUpdateListener toolbarUiUpdateListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "SpotApp, MapsFragment onAttach()");
        activity = (Activity) context;
        toolbarUiUpdateListener = (ToolbarUiUpdateListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
        spotDataImageCaptureHashMap = new LinkedHashMap<>();
        new BitmapFactory.Options().inSampleSize = Constants.BITMAP_OPTIONS_SAMPLE_SIZE;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable("cameraMediaOutputUri", mImageCaptureUri);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "SpotApp, MapsFragment onActivityCreated()");
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("cameraMediaOutputUri")) {
                mImageCaptureUri = savedInstanceState.getParcelable("cameraMediaOutputUri");
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        this.mViewGroup = container;
        ButterKnife.bind(this, rootView);

        mFirebaseConnectivityView = rootView.findViewById(R.id.firebaseConnectionTextView);
        View bottomSheetLastReportView = rootView.findViewById(R.id.dataReportLayout);
        mPotHoleImageView = bottomSheetLastReportView.findViewById(R.id.potHoleImageView);
        mAccidentImageView = bottomSheetLastReportView.findViewById(R.id.accidentImageView);
        mPotHoleTextView = bottomSheetLastReportView.findViewById(R.id.potHoleTextView);
        mAccidentTextView = bottomSheetLastReportView.findViewById(R.id.accidentTextView);

        mapFragment = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        // Get email id information from shared preferences
        mUserEmailId = SharedPrefsUtils.getStringPreference(activity.getApplicationContext(), getString(R.string.user_email_id));
        connectionReference = FirebaseDatabase.getInstance().getReference(".info/connected");
        if (mUserEmailId != null) {
            String encodedEmailId = StringUtils.encodeUserEmail(mUserEmailId);
            userDataReference = FirebaseDatabase.getInstance().getReference(encodedEmailId);
            storageReference = FirebaseStorage.getInstance().getReference("images/" + encodedEmailId);
        }

        mSimpleDateFormatForCloudDataPush = new SimpleDateFormat(getString
                (R.string.date_time_format_for_cloud_data_push));

        String lastSentTimeStamp = readTimeStampFromSharedPreferences();
        if (toolbarUiUpdateListener != null) {
            if (lastSentTimeStamp != null) {
                toolbarUiUpdateListener.updateToolbarSubtitle(
                        getString(R.string.toolbar_subtitle_prefix) + lastSentTimeStamp);
            } else {
                toolbarUiUpdateListener.updateToolbarSubtitle(
                        getString(R.string.default_toolbar_subtitle));
            }
        }

        mPotHoleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePotHoleImageView(true);
                updatePotHoleTextView(true);
                updateAccidentImageView(false);
                updateAccidentTextView(false);
                reportingType = Constants.POTHOLE_TYPE;
            }
        });

        mAccidentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccidentImageView(true);
                updateAccidentTextView(true);
                updatePotHoleImageView(false);
                updatePotHoleTextView(false);
                reportingType = Constants.ACCIDENT_TYPE;
            }
        });

        // Moves map camera to user's current location
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLatitude != 0 && mCurrentLongitude != 0) {
                    moveMapCameraToMarkerPosition(mCurrentLatitude, mCurrentLongitude);
                    updateMarkerPositionOnMap(new LatLng(mCurrentLatitude, mCurrentLongitude));
                }
            }
        });

        // Creates SpotData object to initiates data posting to firebase
        tapFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotData spotData = createSpotData();
                if (spotData != null) {
                    sendToFireBase(spotData);
                    if (toastMessage != null) {
                        toastMessage.cancel();
                    }
                    toastMessage = Toast.makeText(activity.getApplicationContext(), R.string.report_sent_toast, Toast.LENGTH_SHORT);
                    toastMessage.show();
                }
                synchronized (this) {
                    if (addDataPointByDraggingOrSearch) {
                        addDataPointByDraggingOrSearch = false;
                    }
                }
                if (mCurrentLatitude != 0 && mCurrentLongitude != 0) {
                    moveMapCameraToUserLocation();
                }
            }
        });

        captureFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        return rootView;
    }

    ValueEventListener connectionEventChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            boolean connected = snapshot.getValue(Boolean.class);
            Log.d(TAG, "SpotApp, connectionReference onDataChange(), conn: " + connected);
            if (getActivity() != null && isAdded()) {
                if (connected) {
                    mFirebaseConnectivityView.setBackgroundColor(getResources()
                            .getColor(R.color.firebase_connected));
                    mFirebaseConnectivityView.setText(R.string.firebase_connection_message);
                } else {
                    mFirebaseConnectivityView.setBackgroundColor(getResources()
                            .getColor(R.color.firebase_disconnected));
                    mFirebaseConnectivityView.setText(R.string.firebase_disconnection_message);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
        }
    };

    ValueEventListener userDataChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "SpotApp, userDataRef onDataChange(), data: " + dataSnapshot.toString());
            if (dataSnapshot.getValue() != null) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    SpotData spotData
                            = gson.fromJson((String) child.getValue(), SpotData.class);
                    if (activity != null && isAdded()) {
                        if (toolbarUiUpdateListener != null) {
                            toolbarUiUpdateListener.updateToolbarSubtitle(
                                    getString(R.string.toolbar_subtitle_prefix) + spotData.getTimeStamp());
                            writeTimeStampToSharedPreferences(spotData.getTimeStamp());
                        }
                    }
                }
            } else {
                if (activity != null && isAdded()) {
                    if (toolbarUiUpdateListener != null) {
                        toolbarUiUpdateListener.updateToolbarSubtitle(
                                getString(R.string.default_toolbar_subtitle));
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            //Handle possible errors.
        }
    };

    // Updates pothole image view when user selects or deselects it
    private void updatePotHoleImageView(boolean select) {
        if (select) {
            mPotHoleImageView.setBackground(getResources()
                    .getDrawable(R.drawable.image_view_selected));
            mPotHoleImageView.setImageResource(R.drawable.ic_pothole_white);
        } else {
            mPotHoleImageView.setBackground(getResources()
                    .getDrawable(R.drawable.image_view_not_selected));
            mPotHoleImageView.setImageResource(R.drawable.ic_pothole_black);
        }
    }

    // Updates accident image view when user selects or deselects it
    private void updateAccidentImageView(boolean select) {
        if (select) {
            mAccidentImageView.setBackground(getResources()
                    .getDrawable(R.drawable.image_view_selected));
            mAccidentImageView.setImageResource(R.drawable.ic_accident_white);
        } else {
            mAccidentImageView.setBackground(getResources()
                    .getDrawable(R.drawable.image_view_not_selected));
            mAccidentImageView.setImageResource(R.drawable.ic_accident_black);
        }
    }

    // Updates pothole text view color when user selects or deselects pothole image view
    private void updatePotHoleTextView(boolean select) {
        if (select) {
            mPotHoleTextView.setTextColor(getResources().getColor(R.color.blue_600));
        } else {
            mPotHoleTextView.setTextColor(getResources().getColor(R.color.grey_800));
        }
    }

    // Updates accident text view color when user selects or deselects accident image view
    private void updateAccidentTextView(boolean select) {
        if (select) {
            mAccidentTextView.setTextColor(getResources().getColor(R.color.blue_600));
        } else {
            mAccidentTextView.setTextColor(getResources().getColor(R.color.grey_800));
        }
    }

    // Creates and returns SpotData object if gps is fixed, null otherwise
    private SpotData createSpotData() {
        String currentDateTime = mSimpleDateFormatForCloudDataPush.format(Calendar.getInstance().getTime());
        if (mCurrentLatitude == 0 && mCurrentLongitude == 0) {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            toastMessage = Toast.makeText(activity.getApplicationContext(),
                    R.string.gps_not_fiexed_toast, Toast.LENGTH_SHORT);
            toastMessage.show();
            return null;
        }
        return new SpotData(mapMarker.getPosition().latitude, mapMarker.getPosition().longitude,
                currentDateTime, reportingType);
    }

    // Starts intent service to push data to firebase
    private void sendToFireBase(SpotData dataObject) {
        if (firebaseDataPost != null) {
            firebaseDataPost.pushDataToCloud(dataObject);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            photoFile = FileUtils.createJpegImageFile(activity.getApplication());

            SpotData spotData = createSpotData();
            String imageFileName = StringUtils.getFileNameFromFilePath(photoFile.toString());
            Log.d(TAG, "SpotApp, MapsFragment dispatchTakePictureIntent() imageFileName: " + imageFileName);

            if (spotData != null) {
                spotDataImageCaptureHashMap.put(imageFileName, spotData);
                // Continue only if the File was successfully created
                mImageCaptureUri = FileUtils.getFileUriFromFileProvider(activity.getApplication(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                activity.startActivityForResult(takePictureIntent, Constants.CAMERA_CAPTURE_INTENT_REQUEST_CODE);
            } else {
                Log.e(TAG, "SpotApp, MapsFragment dispatchTakePictureIntent() hashmap is null");
            }
        }
    }

    /*
     * Receives location updates from GPS Service
     */
    private BroadcastReceiver locationMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SpotApp, MapsFragment mLocationMessageReceiver() onReceive");
            if (activity != null) {
                mCurrentLatitude = intent.getDoubleExtra("latitude", 0);
                mCurrentLongitude = intent.getDoubleExtra("longitude", 0);

                // Moves map camera to user's current location only once
                if (!isMapCameraMovedToMyLocation) {
                    myLocationButton.setVisibility(View.VISIBLE);
                    setMarkerOnUserLocation(mCurrentLatitude, mCurrentLongitude);
                    moveMapCameraToMarkerPosition(mCurrentLatitude, mCurrentLongitude);
                    isMapCameraMovedToMyLocation = true;
                } else {
                    synchronized (this) {
                        if (!addDataPointByDraggingOrSearch) {
                            updateMarkerPositionOnMap(new LatLng(mCurrentLatitude, mCurrentLongitude));
                        }
                    }
                }
            }
        }
    };

    /*
     * Gets last report timestamp information from shared preferences
     */
    private String readTimeStampFromSharedPreferences() {
        return SharedPrefsUtils.getStringPreference(activity.getApplicationContext(),
                getString(R.string.last_report_time_stamp));
    }

    /*
     * Writes last report timestamp information to shared preferences
     */
    private void writeTimeStampToSharedPreferences(String timeStamp) {
        SharedPrefsUtils.setStringPreference(activity.getApplicationContext(),
                getString(R.string.last_report_time_stamp), timeStamp);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.d(TAG, "SpotApp, MapsFragment onMapLongClick");
            }
        });

        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.d(TAG, "SpotApp, MapsFragment onMarkerDragStart");
                synchronized (this) {
                    addDataPointByDraggingOrSearch = true;
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mapMarker = marker;
            }
        });
    }

    /*
     * When a location update is received, clear everything and put a new marker
     */
    private void setMarkerOnUserLocation(double latitude, double longitude) {
        Log.d(TAG, "SpotApp, MapsFragment setMarkerOnMap()");
        if (mGoogleMap != null) {
            mGoogleMap.clear();
            LatLng latLng = new LatLng(latitude, longitude);
            mapMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        }
    }

    private void updateMarkerPositionOnMap(LatLng latLng) {
        Log.d(TAG, "SpotApp, MapsFragment updateMarkerPositionOnMap()");
        if (mGoogleMap != null) {
            mapMarker.setPosition(latLng);
        }
    }

    /*
     *  Moves map camera to user's current location
     */
    private void moveMapCameraToMarkerPosition(double latitude, double longitude) {
        if (mGoogleMap != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude, longitude), mDefaultZoomInLevel));
        }
    }

    /*
     *  Moves map camera to user's current location
     */
    private void animateMapCamera(LatLng latLng, int duration) {
        if (mGoogleMap != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
                    mDefaultZoomInLevel), duration, null);
        }
    }

    /*
     *  Covers entire map fragment view with black transparent overlay when appbar is fully expanded
     *  and disables all touch events on the map fragment
     */
    public void coverBlackOverLay() {
        transparentView.setBackgroundColor(Color.parseColor("#C0000000"));
        ViewUtils.enableDisableViewGroup(mViewGroup, false);
    }

    /*
     *  Removes black transparent overlay from fragment view when appbar is fully collapsed
     *  and re-enables all touch events on the map fragment
     */
    public void removeBlackOverLay() {
        transparentView.setBackgroundColor(Color.parseColor("#00ffffff"));
        ViewUtils.enableDisableViewGroup(mViewGroup, true);
    }

    public void moveMapCameraToUserLocation() {
        synchronized (this) {
            addDataPointByDraggingOrSearch = false;
        }
        updateMarkerPositionOnMap(new LatLng(mCurrentLatitude, mCurrentLongitude));
        moveMapCameraToMarkerPosition(mCurrentLatitude, mCurrentLongitude);
    }

    public void moveMapCameraToSearchLocation(Place place) {
        synchronized (this) {
            addDataPointByDraggingOrSearch = true;
        }
        updateMarkerPositionOnMap(place.getLatLng());
        animateMapCamera(place.getLatLng(), 500);
    }

    public void cameraCaptureCancel() {
        if (activity != null && isAdded()) {
            if (spotDataImageCaptureHashMap != null && spotDataImageCaptureHashMap.size() > 0) {
                String lastKey = null;
                Iterator<String> iterator = spotDataImageCaptureHashMap.keySet().iterator();
                while (iterator.hasNext()) {
                    lastKey = iterator.next();
                }
                spotDataImageCaptureHashMap.remove(lastKey);
                Log.d(TAG, "SpotApp, MapsFragment, cameraCaptureCancel() hashmap aft:\n" +
                        spotDataImageCaptureHashMap.toString());
            }
            SharedPrefsUtils.saveMapPreference(activity.getApplicationContext(), gson,
                    getString(R.string.upload_tasks_hashmap), spotDataImageCaptureHashMap);
        }
    }

    private void retrieveDataFromPreferenceAndInitiateUploadTask() {
        LinkedHashMap<String, SpotData> hashMap = SharedPrefsUtils.getMapPreference(
                activity.getApplicationContext(), gson, getString(R.string.upload_tasks_hashmap));
        if (hashMap != null) {
            Log.d(TAG, "SpotApp, MapsFragment, onStart(), hashmap: " + hashMap.toString());
            spotDataImageCaptureHashMap = hashMap;
            if (spotDataImageCaptureHashMap.size() > 0) {
                if (firebaseDataPost != null) {
                    if (toolbarUiUpdateListener != null) {
                        toolbarUiUpdateListener.showUploadFileMenuItem(true);
                    }
                    firebaseDataPost.pushImageToCloud(spotDataImageCaptureHashMap);
                }
            }
        }
    }

    public void pauseAllPendingTasks() {
        Log.d(TAG, "SpotApp, MapsFragment, pauseAllPendingTasks()");
        if (storageReference != null) {
            List<UploadTask> uploadTasksList = storageReference.getActiveUploadTasks();
            int totalTasks = 0;
            for (UploadTask task : uploadTasksList) {
                if (!task.isComplete() || task.isInProgress()) {
                    task.cancel();
                    totalTasks++;
                }
            }
            Log.d(TAG, "SpotApp, MapsFragment, pauseAllPendingTasks(), pending tasks: " + totalTasks);
        }
        if (spotDataImageCaptureHashMap != null) {
            if (spotDataImageCaptureHashMap.size() <= 0) {
                spotDataImageCaptureHashMap.clear();
            }
            SharedPrefsUtils.saveMapPreference(activity.getApplicationContext(), gson,
                    getString(R.string.upload_tasks_hashmap), spotDataImageCaptureHashMap);
        }
    }

    public void discardAllUserData() {
        Log.d(TAG, "SpotApp, MapsFragment, discardAllUserData()");
        discardUserData = true;
        if (storageReference != null) {
            List<UploadTask> uploadTasksList = storageReference.getActiveUploadTasks();
            int totalTasks = 0;
            for (UploadTask task : uploadTasksList) {
                if (!task.isComplete() || task.isInProgress()) {
                    task.cancel();
                    totalTasks++;
                }
            }
            Log.d(TAG, "SpotApp, MapsFragment, discardAllPendingTasks(), pending tasks: " + totalTasks);
        }
        if (spotDataImageCaptureHashMap != null) {
            spotDataImageCaptureHashMap.clear();
            spotDataImageCaptureHashMap = null;
        }
        SharedPrefsUtils.clearPreferences(activity.getApplicationContext());
    }

    @Override
    public void onUploadSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        if (activity != null && isAdded()) {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            toastMessage = Toast.makeText(activity.getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT);
            toastMessage.show();
            if (taskSnapshot.getMetadata() != null) {
                String fileName = taskSnapshot.getMetadata()
                        .getCustomMetadata("name");
                if (spotDataImageCaptureHashMap.containsKey(fileName)) {
                    if (firebaseDataPost != null) {
                        SpotData spotData = spotDataImageCaptureHashMap
                                .get(fileName);
                        spotData.setUrl(taskSnapshot.getMetadata()
                                .getDownloadUrl().toString());
                        firebaseDataPost.pushDataToCloud(spotData);
                    }
                    /*Log.d(TAG, "Spot, internal files bef: " +
                            Arrays.toString(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).list()));*/
                    FileUtils.deleteImageFiles(activity.getApplication(), fileName);
                    spotDataImageCaptureHashMap.remove(fileName);
                    Log.d(TAG, "SpotApp, MapsFragment, after suck(): "
                            + spotDataImageCaptureHashMap.toString());
                   /* Log.d(TAG, "Spot, internal files aft: " +
                            Arrays.toString(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).list()));*/

                }
            }
        }
        if (spotDataImageCaptureHashMap != null && spotDataImageCaptureHashMap.size() <= 0) {
            onUploadsComplete();
        }
    }

    @Override
    public void onUploadFileNotPresent(String fileName) {
        Log.d(TAG, "SpotApp, MapsFragment, onUploadFileNotPresent(): " + fileName);
        if (toolbarUiUpdateListener != null && spotDataImageCaptureHashMap.size() <= 0) {
            toolbarUiUpdateListener.showUploadFileMenuItem(false);
        }
    }

    @Override
    public void onUploadFailure() {
        if (activity != null && isAdded()) {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            toastMessage = Toast.makeText(activity.getApplicationContext(), "Uploading failed!", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
    }

    @Override
    public void onUploadsComplete() {
        Log.d(TAG, "SpotApp, MapsFragment, onUploadsComplete()");
        if (toolbarUiUpdateListener != null) {
            toolbarUiUpdateListener.showUploadFileMenuItem(false);
        }
    }

    private void cleanUpAllListeners() {
        if (firebaseDataPost != null) {
            firebaseDataPost.setListener(null);
            firebaseDataPost = null;
        }
        if (connectionReference != null) {
            connectionReference.removeEventListener(connectionEventChangeListener);
        }
        if (userDataReference != null) {
            userDataReference.removeEventListener(userDataChangeListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "SpotApp, MapsFragment, onStart()");
        isMapCameraMovedToMyLocation = false;
        addDataPointByDraggingOrSearch = false;
        try {
            LocalBroadcastManager.getInstance(activity).registerReceiver(
                    locationMessageReceiver, new IntentFilter(Constants.LOCATION_BROADCAST_KEY));
        } catch (Exception ignore) {
        }
        if (firebaseDataPost == null && mUserEmailId != null) {
            firebaseDataPost = new FirebaseDataPost(activity, userDataReference, gson, storageReference);
            firebaseDataPost.setListener(this);
        }

        connectionReference.addValueEventListener(connectionEventChangeListener);
        if (mUserEmailId != null) {
            Query lastQuery = userDataReference.orderByKey().limitToLast(1);
            lastQuery.addValueEventListener(userDataChangeListener);
        }
        retrieveDataFromPreferenceAndInitiateUploadTask();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "SpotApp, MapsFragment, onStop()");
        if (!discardUserData) {
            pauseAllPendingTasks();
        }
        isMapCameraMovedToMyLocation = false;
        addDataPointByDraggingOrSearch = false;
        try {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(locationMessageReceiver);
        } catch (Exception ignore) {
        }
        cleanUpAllListeners();
        if (spotDataImageCaptureHashMap != null) {
            spotDataImageCaptureHashMap.clear();
            spotDataImageCaptureHashMap = null;
        }
        if (toastMessage != null) {
            toastMessage.cancel();
        }
        discardUserData = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SpotApp, MapsFragment, onDestroy()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "SpotApp, MapsFragment, onDestroyView()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "SpotApp, MapsFragment, onLowMemory()");
        mapFragment.onLowMemory();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "SpotApp, MapsFragment, onDetach()");
        if (toolbarUiUpdateListener != null) {
            toolbarUiUpdateListener = null;
        }
        super.onDetach();
    }
}
