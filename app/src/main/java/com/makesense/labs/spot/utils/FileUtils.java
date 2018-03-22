package com.makesense.labs.spot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by tango on 15/3/18.
 */

public class FileUtils {

    public static File createJpegImageFile(Context context) {
        // Create an image file name
        String imageFileName = String.valueOf(System.currentTimeMillis());
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(
                storageDir, /* directory */
                imageFileName + ".jpg"
        );
    }

    private static File createWebpImageFile(Context context, String filename) {
        // Create an image file name
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(
                storageDir, /* directory */
                filename + ".webp"
        );
    }

    private static String getFullFilePath(Context context, String filename) {
        // Create an image file name
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            return storageDir.getAbsolutePath() + "/" + filename + ".jpg";
        }
        return null;
    }

    public static boolean checkFileIsPresent(Context context, String filename) {
        // Create an image file name
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, filename + ".jpg").exists();
    }

    public static Uri getFileUriFromFileProvider(Context context, File file) {
        return FileProvider.getUriForFile(context,
                Constants.PACKAGE_NAME + ".fileprovider",
                file);
    }

    public static Uri getResizedAndCompressedBitmapFileUri(Context context,
                                                           String filename) {

        String fullFile = getFullFilePath(context, filename);
        if (fullFile != null) {
            File file = new File(fullFile);
            if (file.exists() && file.length() > 0) {
                Uri imageFileUri = getFileUriFromFileProvider(context, file);
                Bitmap capturedImage;
                File compressedImage;

                try {
                    capturedImage = MediaStore.Images.Media
                            .getBitmap(context.getContentResolver(), imageFileUri);
                    Bitmap resized = Bitmap.createScaledBitmap(capturedImage,
                            Constants.BITMAP_RESIZE_WIDTH, Constants.BITMAP_RESIZE_HEIGHT, true);
                    String capturedImageFilename = imageFileUri.getLastPathSegment();
                    compressedImage = createWebpImageFile(context, capturedImageFilename
                            .substring(0, capturedImageFilename.lastIndexOf(".")));
                    FileOutputStream fileOutputStream = new FileOutputStream(compressedImage, false);
                    resized.compress(Bitmap.CompressFormat.WEBP,
                            Constants.BITMAP_RESIZE_IMAGE_QUALITY, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    resized.recycle();
                    capturedImage.recycle();
                    return getFileUriFromFileProvider(context, compressedImage);
                } catch (IOException ignore) {
                    return null;
                }
            }
        }
        return null;
    }

    public static void deleteImageFiles(Context context, String filename) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            for (File f : storageDir.listFiles()) {
                if (f.getName().startsWith(filename)) {
                    f.delete();
                }
            }
        }
    }

    public static void deleteAllFiles(Context context) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            for (File f : storageDir.listFiles()) {
                f.delete();
            }
        }
    }
}
