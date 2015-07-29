package com.commit451.gitlab.tools;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.LoginActivity;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import retrofit.RetrofitError;
import retrofit.client.Header;

public class RetrofitHelper {
	
	private static final String TAG = "GITLAB-NETWORK-ERROR";
	
	public static void printDebugInfo(Context context, RetrofitError error) {
		if(error == null)
			return;
        else if(error.getCause() instanceof SSLHandshakeException && context != null) {
            Prefs.setLoggedIn(GitLabApp.instance(), false);
            context.startActivity(new Intent(context, LoginActivity.class));
            return;
        }

        String errorMessage = "";
        String temp;

        temp = "URL: " + error.getUrl();
		Log.e(TAG, temp);
        errorMessage += temp + "\n";

		if(error.getResponse() != null) {
			temp = "Status: " + error.getResponse().getStatus() + " - " + error.getResponse().getReason();
            Log.e(TAG, temp);
            errorMessage += temp + "\n";

			ArrayList<Header> headers = new ArrayList<Header>(error.getResponse().getHeaders());
			for(Header h : headers) {
                temp = "Header: " + h.getName() + " - " + h.getValue();
				Log.e(TAG, temp);
                errorMessage += temp + "\n";
			}
			
			try {
				if(error.getResponse().getBody() != null && error.getResponse().getBody().in() != null) {
                    temp = "Body: " + IOUtils.toString(error.getResponse().getBody().in());
                    Log.e(TAG, temp);
                    errorMessage += temp + "\n";
                }
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

        temp = "Message: " + error.getMessage();
		Log.e(TAG, temp);
        errorMessage += temp + "\n";

        errorMessage += ExceptionUtils.getStackTrace(error);
		error.printStackTrace();

        if(error.getCause() instanceof SocketTimeoutException || error.getCause() instanceof UnknownHostException || error.getCause() instanceof ConnectException) {
            return;
        }
	}
}
