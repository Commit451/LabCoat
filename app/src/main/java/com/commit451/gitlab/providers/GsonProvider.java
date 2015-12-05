package com.commit451.gitlab.providers;

import android.net.Uri;

import com.commit451.gitlab.api.UriConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Get a properly configured Gson object
 * Created by Jawn on 12/4/2015.
 */
public class GsonProvider {

    private static Gson sGson;
    public static Gson getInstance() {
        if (sGson == null) {
            sGson = createInstance();
        }
        return sGson;
    }

    public static Gson createInstance() {
        // Configure Gson to handle dates correctly
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                return ISODateTimeFormat.dateTimeParser().parseDateTime(json.getAsString()).toDate();
            }
        });
        gsonBuilder.registerTypeAdapter(Uri.class, UriConverter.getDeserializer());
        return gsonBuilder.create();
    }
}
