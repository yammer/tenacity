package com.yammer.tenacity.core.helper;

import com.sun.jersey.api.client.UniformInterfaceException;

import java.util.Objects;

@Deprecated
public class ClientException extends RuntimeException{
    private final static long serialVersionUID = -1237219873892173912L;
    private final int statusCode;

    public ClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ClientException(UniformInterfaceException cause) {
        super(cause);
        this.statusCode = cause.getResponse().getStatus();
    }

    public int getStatus(){
        return statusCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientException that = (ClientException) o;

        return Objects.equals(this.statusCode, that.statusCode) && Objects.equals(this.getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, getMessage());
    }
}
