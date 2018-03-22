package com.makesense.labs.spot.utils;

/**
 * Defines several constants used between GPSService and the UI.
 */
public final class Constants {

    public static final String PACKAGE_NAME = "com.makesense.labs.spot";

    // Message types sent from the Service Handler
    public static final int LOCATION_START = 1;

    public static final int POTHOLE = 0;
    public static final int ACCIDENT = 1;
    public static final String POTHOLE_TYPE = "pot";
    public static final String ACCIDENT_TYPE = "acc";

    public static final int LOCATION_INTERVAL = 5000;
    public static final int LOCATION_FASTEST_INTERVAL = 1000;

    public static final String LOCATION_BROADCAST_KEY = PACKAGE_NAME + ".LOCATION_BROADCAST_KEY";
    public static final String CONNECTIVITY_BROADCAST_KEY = PACKAGE_NAME + ".CONNECTIVITY_BROADCAST_KEY";
    public static final String CONNECTIVITY_CHANGED_INTENT = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String INTERNET_AVAILABLE_BROADCAST_KEY = PACKAGE_NAME + ".INTERNET_AVAILABLE_BROADCAST_KEY";

    static final int BITMAP_RESIZE_WIDTH = 640;
    static final int BITMAP_RESIZE_HEIGHT = 480;
    static final int BITMAP_RESIZE_IMAGE_QUALITY = 60;
    public static final int BITMAP_OPTIONS_SAMPLE_SIZE = 4;

    public static final int LOCATION_ENABLE_INTENT_REQUEST_CODE = 1122;
    public static final int PLACES_AUTOCOMPLETE_REQUEST_CODE = 101;

    /*
     * Runtime permissions related constants
     */
    public static final int LOCATION_PERMISSION_CHECK_REQUEST_CODE = 120;
    public static final int LOCATION_PERMISSION_CHANGE_REQUEST_CODE = 121;
    public static final int APP_PERMISSIONS_ALLOW_REQUEST_CODE = 150;
    public static final int APP_PERMISSIONS_CHANGE_REQUEST_CODE = 151;
    public static final int CAMERA_CAPTURE_INTENT_REQUEST_CODE = 152;

    public static final int FIRE_BASE_SIGN_IN = 123;

}
