package com.commit451.gitlab.api;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Intercepts requests and logs them using Timber
 * Created by John on 9/11/15.
 */
public class TimberRequestInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        Timber.i("Sending request %s%n%s",
                request.url(), request.headers());

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        Timber.i("Received response for %s in %.1fms%n%s%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers(), response.body());

        return response;
    }
}