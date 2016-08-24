package com.commit451.gitlab.observable;

import com.commit451.gitlab.util.FileUtil;

import java.io.File;

import okhttp3.MultipartBody;
import rx.Observable;
import rx.functions.Func0;

/**
 * Rx'ifies file util calls
 */
public class FileObservableFactory {

    public static Observable<MultipartBody.Part> toPart(final File file) {
        return Observable.defer(new Func0<Observable<MultipartBody.Part>>() {
            @Override
            public Observable<MultipartBody.Part> call() {
                return Observable.just(FileUtil.toPart(file));
            }
        });
    }
}
