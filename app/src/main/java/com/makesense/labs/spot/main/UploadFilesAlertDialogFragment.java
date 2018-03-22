package com.makesense.labs.spot.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.makesense.labs.spot.R;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 * @description DialogFragment displays logout alert message
 * and waits for user approval before performing an action
 */
public class UploadFilesAlertDialogFragment extends DialogFragment {

    private String TAG = "UploadFilesAlertDialogFragment";

    private Context context;
    private UploadFilesActionListener uploadFilesActionListener;

    /**
     * Create a new instance of LogOutAlertDialogFragment
     */
    public static UploadFilesAlertDialogFragment newInstance() {
        return new UploadFilesAlertDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getText(R.string.upload_files_alert_dialog_title));

        builder.setNegativeButton(getText(R.string.pause_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                uploadFilesActionListener.pauseAllPendingTasks();
            }
        });
        builder.setPositiveButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "SpotApp, UploadFilesAlertDialogFragment, onAttach()");
        this.context = context;
        try {
            uploadFilesActionListener = (UploadFilesActionListener) context;
        } catch (ClassCastException ignore) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "SpotApp, UploadFilesAlertDialogFragment, onStart()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "SpotApp, UploadFilesAlertDialogFragment, onStop()");
        try {
            if (getDialog() != null)
                if (getDialog().isShowing()) {
                    getDialog().dismiss();
                }
        } catch (Exception e) {
            Log.e(TAG, "SpotApp, UploadFilesAlertDialogFragment, onStop Exception dialog close");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SpotApp, UploadFilesAlertDialogFragment, onDestroy()");
    }
}

