package com.yammer.tenacity.core.helper;


import javax.ws.rs.client.ResponseProcessingException;
import java.util.Objects;

public class ClientException extends RuntimeException{
    private static final long serialVersionUID = 12886239001277773L;
    private final int statusCode;

    public ClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ClientException(ResponseProcessingException cause) {
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
