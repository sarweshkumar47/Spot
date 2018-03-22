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
public class LogOutAlertDialogFragment extends DialogFragment {

    private String TAG = "LogOutAlertDialogFragment";

    private AppLogOutListener appLogOutListener;
    private Context context;
    private final CharSequence[] logOutOptions = {"Delete my account from database"};
    private boolean mAccountDelete = false;

    /**
     * Create a new instance of LogOutAlertDialogFragment
     */
    public static LogOutAlertDialogFragment newInstance() {
        return new LogOutAlertDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getText(R.string.log_out_title));
        builder.setMultiChoiceItems(logOutOptions, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        mAccountDelete = isChecked;
                    }
                });
        builder.setPositiveButton(getText(R.string.log_out_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // After user confirmation, initiates logout process
                appLogOutListener.onAppSignOutConfirm(mAccountDelete);
            }
        });
        builder.setNegativeButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "SpotApp, LogOutAlertDialogFragment, onAttach()");
        this.context = context;
        try {
            appLogOutListener = (AppLogOutListener) context;
        } catch (ClassCastException ignore) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "SpotApp, LogOutAlertDialogFragment, onStart()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "SpotApp, LogOutAlertDialogFragment, onStop()");
        try {
            if (getDialog() != null)
                if (getDialog().isShowing()) {
                    getDialog().dismiss();
                }
        } catch (Exception e) {
            Log.e(TAG, "SpotApp, LogOutAlertDialogFragment, onStop Exception dialog close");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SpotApp, LogOutAlertDialogFragment, onDestroy()");
    }
}

