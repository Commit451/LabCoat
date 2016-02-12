package com.commit451.gitlab.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.commit451.gitlab.api.exception.HttpException;
import com.commit451.gitlab.api.exception.NullBodyException;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * An easier version of a Retrofit callback that simplifies
 * the response block so that you do not have to check
 * {@link Response#isSuccess()}. You can still call {@link #getResponse()}
 * if you need it. If there is a HTTP error, {@link #onAllFailure(Throwable)}
 * will be called with a {@link HttpException}
 */
public abstract class EasyCallback<T> implements Callback<T> {

    @Nullable
    private Response<T> mResponse;
    @Nullable
    private Retrofit mRetrofit;

    public abstract void onResponse(@NonNull T response);

    public abstract void onAllFailure(Throwable t);

    @Override
    public void onResponse(Response<T> response, Retrofit retrofit) {
        mRetrofit = retrofit;
        mResponse = response;
        if (!response.isSuccess()) {
            onAllFailure(new HttpException(response.code(), response.errorBody()));
        }
        if (response.body() == null) {
            onAllFailure(new NullBodyException());
        }
        onResponse(response.body());
    }

    @Override
    public void onFailure(Throwable t) {
        onAllFailure(t);
    }

    @Nullable
    public Response<T> getResponse() {
        return mResponse;
    }

    @Nullable
    public Retrofit getRetrofit() {
        return mRetrofit;
    }
}
