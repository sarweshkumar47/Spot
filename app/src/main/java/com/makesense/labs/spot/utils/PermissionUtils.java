package com.makesense.labs.spot.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class PermissionUtils {
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean showRequestPermissionRationale(Activity activity, String permission) {
        if (activity != null && permission != null) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        if (activity != null) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }
}
