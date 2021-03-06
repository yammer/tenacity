package com.yammer.tenacity.core.errors;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.SocketTimeoutException;
import java.util.Objects;

public class TenacityExceptionMapper implements ExceptionMapper<HystrixRuntimeException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityExceptionMapper.class);
    private final int statusCode;

    public TenacityExceptionMapper() {
        this(429); // Too Many Requests http://tools.ietf.org/html/rfc6585#section-4
    }

    public TenacityExceptionMapper(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static boolean isTenacityException(Throwable throwable) {
        if (throwable != null && throwable instanceof HystrixRuntimeException) {
            return isTenacityException((HystrixRuntimeException) throwable);
        }
        return false;
    }

    public static boolean isTenacityException(HystrixRuntimeException exception) {
        switch (exception.getFailureType()) {
            case TIMEOUT:
            case SHORTCIRCUIT:
            case REJECTED_THREAD_EXECUTION:
            case REJECTED_SEMAPHORE_EXECUTION:
            case REJECTED_SEMAPHORE_FALLBACK:
                return true;
            case COMMAND_EXCEPTION:
                //TODO: Remove this and set to false by default
                //SocketTimeoutExceptions should be fixed by the application if they are being thrown within the context
                //of a TenacityCommand
                return exception.getCause() instanceof SocketTimeoutException;
            default:
                return false;
        }
    }

    @Override
    public Response toResponse(HystrixRuntimeException exception) {
        if (isTenacityException(exception)) {
            LOGGER.debug("Unhandled HystrixRuntimeException", exception);
            return Response.status(statusCode).build(); //TODO: Retry-After for 429
        }

        LOGGER.warn("HystrixRuntimeException is not mappable to a status code: {}", exception);

        return Response.serverError().build();
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
        final TenacityExceptionMapper other = (TenacityExceptionMapper) obj;
        return Objects.equals(this.statusCode, other.statusCode);
    }
}