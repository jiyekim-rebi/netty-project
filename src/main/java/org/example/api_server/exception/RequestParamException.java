package org.example.api_server.exception;

public class RequestParamException extends RuntimeException{
    public RequestParamException(String message) {
        super(message);
    }
}
