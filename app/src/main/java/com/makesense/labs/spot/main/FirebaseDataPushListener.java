package com.makesense.labs.spot.main;

import com.google.firebase.storage.UploadTask;

/**
 * Created by tango on 21/3/18.
 */

public interface FirebaseDataPushListener  {

    void onUploadSuccess(UploadTask.TaskSnapshot taskSnapshot);

    void onUploadFileNotPresent(String fileName);

    void onUploadFailure();

    void onUploadsComplete();
}
