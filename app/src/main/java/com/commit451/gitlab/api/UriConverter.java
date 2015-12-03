package com.commit451.gitlab.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.data.Prefs;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import android.net.Uri;

import java.lang.reflect.Type;

public class UriConverter {
    private static final Matcher MATCHER = new Matcher() {
        @Override
        public Transform match(Class type) throws Exception {
            if (type.equals(Uri.class)) {
                return new Transform<Uri>() {
                    @Override
                    public Uri read(String value) throws Exception {
                        if (value == null) {
                            return null;
                        }

                        if (value.length() == 0) {
                            return Uri.EMPTY;
                        }

                        return convertUri(value);
                    }

                    @Override
                    public String write(Uri value) throws Exception {
                        if (value == null) {
                            return null;
                        }

                        return value.toString();
                    }
                };
            }

            return null;
        }
    };
    public static Matcher getMatcher() {
        return MATCHER;
    }

    private static final JsonDeserializer<Uri> DESERIALIZER = new JsonDeserializer<Uri>() {
        @Override
        public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonNull()) {
                return null;
            }

            String value = json.getAsString();
            if (value.length() == 0) {
                return Uri.EMPTY;
            }

            return convertUri(value);
        }
    };
    public static JsonDeserializer<Uri> getDeserializer() {
        return DESERIALIZER;
    }

    private static Uri convertUri(String uriString) {
        Uri uri = Uri.parse(uriString);
        if (!uri.isRelative()) {
            return uri;
        }

        Uri.Builder builder = Uri.parse(Prefs.getServerUrl(GitLabApp.instance()))
                .buildUpon()
                .encodedQuery(uri.getEncodedQuery())
                .encodedFragment(uri.getEncodedFragment());

        if (uri.getPath().startsWith("/")) {
            builder.encodedPath(uri.getEncodedPath());
        } else {
            builder.appendEncodedPath(uri.getEncodedPath());
        }

        return builder.build();
    }
}
