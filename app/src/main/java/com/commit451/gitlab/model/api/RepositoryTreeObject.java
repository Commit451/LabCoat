package com.commit451.gitlab.model.api;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Parcel
@JsonObject
public class RepositoryTreeObject {

    public static final String TYPE_FOLDER = "tree";
    public static final String TYPE_REPO = "submodule";
    public static final String TYPE_FILE = "blob";

    @StringDef({TYPE_FOLDER, TYPE_REPO, TYPE_FILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @JsonField(name = "id")
    String id;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "type")
    @Type
    String type;
    @JsonField(name = "mode")
    String mode;

    public RepositoryTreeObject() {}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @Type String getType() {
        return type;
    }

    public String getMode() {
        return mode;
    }
}
