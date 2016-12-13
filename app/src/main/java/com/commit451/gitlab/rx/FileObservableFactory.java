package com.commit451.gitlab.rx;

import com.commit451.gitlab.util.FileUtil;

import java.io.File;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import okhttp3.MultipartBody;

/**
 * Rx'ifies file util calls
 */
public class FileObservableFactory {

    public static Single<MultipartBody.Part> toPart(final File file) {
        return Single.defer(new Callable<SingleSource<? extends MultipartBody.Part>>() {
            @Override
            public SingleSource<? extends MultipartBody.Part> call() throws Exception {
                return Single.just(FileUtil.toPart(file));
            }
        });
    }
}
