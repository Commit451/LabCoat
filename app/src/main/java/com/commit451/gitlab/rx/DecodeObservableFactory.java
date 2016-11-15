package com.commit451.gitlab.rx;

import android.util.Base64;

import rx.Observable;
import rx.functions.Func0;

/**
 * Observable that decodes a byte array
 */
public class DecodeObservableFactory {

    public static Observable<byte[]> newDecode(final String string) {
        return Observable.defer(new Func0<Observable<byte[]>>() {
            @Override
            public Observable<byte[]> call() {
                return Observable.just(decode(string));
            }
        });
    }

    private static byte[] decode(String string) {
        return Base64.decode(string, Base64.DEFAULT);
    }
}
