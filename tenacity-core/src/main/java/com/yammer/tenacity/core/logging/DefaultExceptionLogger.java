package com.yammer.tenacity.core.logging;

import com.netflix.hystrix.HystrixCommand;

/**
 * The simplest exception logger out there, just logs any and every exception
 */
public class DefaultExceptionLogger extends ExceptionLogger<Exception> {

    @Override
    protected <T> void logException(Exception exception, HystrixCommand<T> commandInstance) {
        logger.warn("An exception occurred while executing {}:{}", commandInstance.getCommandKey(), commandInstance.getClass().getSimpleName(), exception);
    }
}
