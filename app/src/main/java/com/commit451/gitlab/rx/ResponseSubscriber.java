package com.commit451.gitlab.rx;

import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

/**
 * Subscriber that sends HTTP error codes into the {@link #onError(Throwable)}
 * block, but also allows access to the Retrofit response. This is useful
 * for cases where you do not need or want to still check {@link Response#isSuccessful()}
 * but also would like to be able to access the Retrofit response in your success block (via {@link #getResponse()}
 */
public abstract class ResponseSubscriber<T> extends Subscriber<Response<T>> {

    private Response response;

    protected abstract void onNextSuccess(T response);

    @Override
    public void onNext(Response<T> response) {
        this.response = response;
        if (!response.isSuccessful()) {
            onError(new HttpException(response));
        } else {
            onNextSuccess(response.body());
        }
    }

    public Response getResponse() {
        return response;
    }
}
