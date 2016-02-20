package com.commit451.gitlab.api.exception;

/**
 * Represents that the body was null
 */
public class NullBodyException extends Exception{

    @Override
    public String getMessage() {
        return "The Retrofit message was null";
    }
}
