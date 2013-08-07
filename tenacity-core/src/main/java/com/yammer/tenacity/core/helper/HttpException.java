package com.yammer.tenacity.core.helper;

import com.sun.jersey.api.client.UniformInterfaceException;

import java.util.Objects;

public class HttpException extends RuntimeException{

    private final int statusCode;

    public HttpException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(String message, int statusCode, UniformInterfaceException cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatus(){
        return statusCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpException that = (HttpException) o;

        return Objects.equals(this.statusCode, that.statusCode) && Objects.equals(this.getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, getMessage());
    }
}
