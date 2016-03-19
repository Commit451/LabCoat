package com.commit451.gitlab.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.commit451.gitlab.api.exception.HttpException;
import com.commit451.gitlab.api.exception.NullBodyException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An easier version of a Retrofit callback that simplifies
 * the response block so that you do not have to check
 * {@link Response#isSuccessful()}. You can still call {@link #getResponse()}
 * if you need it. If there is a HTTP error, {@link #onAllFailure(Throwable)}
 * will be called with a {@link HttpException}
 */
public abstract class EasyCallback<T> implements Callback<T> {

    @Nullable
    private Response<T> mResponse;
    private Call<T> mCall;

    public abstract void onResponse(@NonNull T response);

    public abstract void onAllFailure(Throwable t);

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        mCall = call;
        mResponse = response;
        if (!response.isSuccessful()) {
            onAllFailure(new HttpException(response.code(), response.errorBody()));
            return;
        }
        if (response.body() == null) {
            onAllFailure(new NullBodyException());
            return;
        }
        onResponse(response.body());
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        mCall = call;
        onAllFailure(t);
    }

    @Nullable
    public Response<T> getResponse() {
        return mResponse;
    }

    @Nullable
    public Call<T> getCall() {
        return mCall;
    }
}
