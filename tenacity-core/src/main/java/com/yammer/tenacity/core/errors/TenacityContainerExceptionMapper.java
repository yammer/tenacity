package com.yammer.tenacity.core.errors;


import com.sun.jersey.api.container.ContainerException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Objects;

public class TenacityContainerExceptionMapper implements ExceptionMapper<ContainerException> {
    private final int statusCode;

    public TenacityContainerExceptionMapper() {
        this(429); // Too Many Requests http://tools.ietf.org/html/rfc6585#section-4
    }

    public TenacityContainerExceptionMapper(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public Response toResponse(ContainerException exception) {
        if (TenacityExceptionMapper.isTenacityException(exception.getCause())) {
            return Response.status(statusCode).build();
        } else {
            throw exception;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TenacityContainerExceptionMapper other = (TenacityContainerExceptionMapper) obj;
        return Objects.equals(this.statusCode, other.statusCode);
    }
}
