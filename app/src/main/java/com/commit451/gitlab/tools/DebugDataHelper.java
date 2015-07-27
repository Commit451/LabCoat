package com.commit451.gitlab.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DebugDataHelper {

    private static final String TITLE = "Server Error";
    private static final String MESSAGE = "Your GitLab Server behaved unexpectedly.\nWould you like to send the developer information so he can fix it?";
    private static final String POSITIVE_BUTTON = "OK";
    private static final String NEGATIVE_BUTTON = "Cancel";
    private static final String SUBJECT = "GitLab Server Error";
    private static final String RECIPIENT = "benjamin.dengler@gmail.com";

    public static void sendErrorReport(final Context context, final String errorMessage) {
        new AlertDialog.Builder(context).setTitle(TITLE).setMessage(MESSAGE).setPositiveButton(POSITIVE_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openEmail(context, errorMessage);
                dialog.dismiss();
            }
        }).setNegativeButton(NEGATIVE_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private static void openEmail(Context context, String errorMessage) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String subject = SUBJECT;
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {RECIPIENT});
        sendIntent.putExtra(Intent.EXTRA_TEXT, errorMessage);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("message/rfc822");

        context.startActivity(Intent.createChooser(sendIntent, "Select email client"));
    }
}
