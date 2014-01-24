package com.yammer.tenacity.core.errors;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.sun.jersey.api.container.ContainerException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

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

    private boolean isHystrixRuntimeException(Throwable throwable) {
        if (throwable == null) {
            return false;
        } else if (throwable instanceof HystrixRuntimeException) {
            return true;
        }
        return isHystrixRuntimeException(throwable.getCause());
    }

    @Override
    public Response toResponse(ContainerException exception) {
        if (isHystrixRuntimeException(exception.getCause())) {
            return Response.status(statusCode).build();
        } else {
            throw exception;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenacityContainerExceptionMapper that = (TenacityContainerExceptionMapper) o;

        if (statusCode != that.statusCode) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return statusCode;
    }
}
