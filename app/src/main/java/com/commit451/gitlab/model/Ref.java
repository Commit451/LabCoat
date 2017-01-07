package com.commit451.gitlab.model;

import android.support.annotation.IntDef;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Local only model that references either a branch or a tag, and holds its type
 */
@Parcel
public class Ref {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_BRANCH, TYPE_TAG})
    public @interface Type {}
    public static final int TYPE_BRANCH = 0;
    public static final int TYPE_TAG = 1;

    int type;
    String ref;

    protected Ref() {

    }

    public Ref(@Type int type, String ref) {
        this.type = type;
        this.ref = ref;
    }

    public int getType() {
        return type;
    }

    public String getRef() {
        return ref;
    }
}
