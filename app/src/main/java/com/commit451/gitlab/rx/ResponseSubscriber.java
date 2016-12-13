package com.commit451.gitlab.rx;

import com.commit451.reptar.retrofit.ResponseSingleObserver;

import retrofit2.Response;


public abstract class ResponseSubscriber<T> extends ResponseSingleObserver<Response<T>> {


}
