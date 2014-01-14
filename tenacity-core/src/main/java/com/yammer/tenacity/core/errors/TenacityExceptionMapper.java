package com.yammer.tenacity.core.errors;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class TenacityExceptionMapper implements ExceptionMapper<HystrixRuntimeException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityExceptionMapper.class);
    private final int statusCode;

    public TenacityExceptionMapper() {
        this(429); // Too Many Requests http://tools.ietf.org/html/rfc6585#section-4
    }

    public TenacityExceptionMapper(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public Response toResponse(HystrixRuntimeException exception) {
        switch (exception.getFailureType()) {
            case TIMEOUT:
            case SHORTCIRCUIT:
            case REJECTED_THREAD_EXECUTION:
            case REJECTED_SEMAPHORE_EXECUTION:
            case REJECTED_SEMAPHORE_FALLBACK:
                LOGGER.info("Unhandled HystrixRuntimeException", exception);
                return Response.status(statusCode).build(); //TODO: Retry-After for 429
            case COMMAND_EXCEPTION:
            default:
                throw new WebApplicationException(exception);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenacityExceptionMapper that = (TenacityExceptionMapper) o;

        if (statusCode != that.statusCode) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return statusCode;
    }
}
