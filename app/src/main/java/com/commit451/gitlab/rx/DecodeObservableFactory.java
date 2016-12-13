package com.commit451.gitlab.rx;

import android.util.Base64;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleSource;

/**
 * Observable that decodes a byte array
 */
public class DecodeObservableFactory {

    public static Single<byte[]> newDecode(final String string) {
        return Single.defer(new Callable<SingleSource<? extends byte[]>>() {
            @Override
            public SingleSource<? extends byte[]> call() throws Exception {
                return Single.just(decode(string));
            }
        });
    }

    private static byte[] decode(String string) {
        return Base64.decode(string, Base64.DEFAULT);
    }
}
