package com.makesense.labs.spot.utils;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class StringUtils {

    public static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public static String decodeUserEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

    public static String getFileNameFromFilePath(String filePath) {
        int index = filePath.lastIndexOf("/");
        String fileName = filePath.substring(index + 1);
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
