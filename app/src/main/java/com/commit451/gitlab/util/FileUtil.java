package com.commit451.gitlab.util;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * Utility methods for uploading files
 */
public class FileUtil {

    public static MultipartBody.Part toPart(Context context, Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            String fileName = getFileName(context, imageUri);
            return toPart(bitmap, fileName);
        } catch (IOException e) {
            //this won't happen, maybe
            Timber.e(e);
        }
        return null;
    }

    public static MultipartBody.Part toPart(Bitmap bitmap, String name) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), stream.toByteArray());
        return MultipartBody.Part.createFormData("file", name, requestBody);
    }

    public static String getFileName(Context context, Uri imageUri) {
        Cursor returnCursor =
                context.getContentResolver().query(imageUri, null, null, null, null);

        if (returnCursor == null) {
            //This should probably just return null, but oh well
            return "file";
        }
        int nameIndex = returnCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        if (nameIndex == -1) {
            nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        }
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        if (!returnCursor.isClosed()) {
            returnCursor.close();
        }
        return name;
    }
}
