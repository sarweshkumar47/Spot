package com.makesense.labs.spot;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class ApplicationController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}