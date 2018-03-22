package com.makesense.labs.spot.main;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.makesense.labs.spot.model.SpotData;
import com.makesense.labs.spot.utils.FileUtils;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
class FirebaseDataPost {

    String TAG = "FirebaseDataPost";

    private final DatabaseReference firebaseDatabaseReference;
    private final Gson gson;
    private final Context context;
    private final Activity activity;
    private StorageReference firebaseStorageReference;
    private FirebaseDataPushListener listener;
    private WeakReference<LinkedHashMap<String, SpotData>> linkedHashMapWeakReference;

    FirebaseDataPost(Context context, DatabaseReference firebaseDatabaseReference, Gson gson,
                     StorageReference storageReference) {
        this.firebaseDatabaseReference = firebaseDatabaseReference;
        this.gson = gson;
        this.context = context;
        this.firebaseStorageReference = storageReference;
        this.activity = (Activity) context;
    }

    void pushDataToCloud(SpotData spotData) {
        Log.i(TAG, "SpotApp, FirebaseDataPost, pushToCloud()");
        String userId = firebaseDatabaseReference.push().getKey();
        String jsonData = gson.toJson(spotData);
        firebaseDatabaseReference.child(userId).setValue(jsonData);
    }

    void pushImageToCloud(LinkedHashMap<String, SpotData> hashMap) {
        if (context != null) {
            linkedHashMapWeakReference = new WeakReference<>(hashMap);
            Iterator<String> iterator = linkedHashMapWeakReference.get().keySet().iterator();
            while (iterator.hasNext()) {
                String fileName = iterator.next();
                Log.d(TAG, "Spot, filename: " + fileName);

                if (FileUtils.checkFileIsPresent(context, fileName)) {
                    Uri uploadFileUri = FileUtils.getResizedAndCompressedBitmapFileUri(
                            context, fileName);
                    if (uploadFileUri != null) {
                        UploadTask uploadTask = firebaseStorageReference.child(fileName)
                                .putFile(uploadFileUri,
                                        new StorageMetadata
                                                .Builder().setContentType("image/webp")
                                                .setCustomMetadata("name", fileName)
                                                .build());

                        uploadTask.addOnSuccessListener(activity, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if (listener != null) {
                                    listener.onUploadSuccess(taskSnapshot);
                                }
                            }
                        });

                        uploadTask.addOnFailureListener(activity, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (listener != null) {
                                    listener.onUploadFailure();
                                }
                            }
                        });
                    }
                } else {
                    iterator.remove();
                    Log.e(TAG, "Spot, file is not present, filename: " + fileName);
                    if (listener != null) {
                        listener.onUploadFileNotPresent(fileName);
                    }
                }
            }
        }
    }

    void setListener(FirebaseDataPushListener listener) {
        this.listener = listener;
    }
}
