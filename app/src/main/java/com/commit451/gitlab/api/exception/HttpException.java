package com.commit451.gitlab.api.exception;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

/**
 * Represents an HTTP non 200 response from Retrofit
 */
public class HttpException extends Exception {

    private int mCode;
    private ResponseBody mErrorBody;

    public HttpException(int code, ResponseBody errorBody) {
        mCode = code;
        mErrorBody = errorBody;
    }

    @Override
    public String getMessage() {
        try {
            return mErrorBody.string();
        } catch (IOException e) {
            return e.toString();
        }
    }

    public ResponseBody getResponseBody() {
        return mErrorBody;
    }

    public int getCode() {
        return mCode;
    }
}