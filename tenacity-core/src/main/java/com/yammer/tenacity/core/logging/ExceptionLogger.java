package com.yammer.tenacity.core.logging;

import com.netflix.hystrix.HystrixCommand;
import io.dropwizard.util.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

/**
 * Logs exceptions that are of (or a subclass of) the specified class
 *
 * Usage should be:
 * <ol>
 *     <li>Check that the ExceptionLogger can handle the type of exception through canHandleException()</li>
 *     <li>Log the exception with log(), if canHandleException() said that it could</li>
 * </ol>
 */
public abstract class ExceptionLogger<E extends Exception> {

    protected final Class<E> clazz;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Specify the class that this exception logger can handle
     */
    protected ExceptionLogger() {
        this.clazz = Generics.getTypeParameter(this.getClass(), Exception.class);
    }

    /**
     * Make sure you check that this ExceptionLogger can actually handle the type of exception
     */
    public boolean canHandleException(Exception exception) {
        return this.clazz.isInstance(exception);
    }

    @SuppressWarnings("unchecked")
    /**
     * Actually log the exception
     * @throws IllegalStateException it relieves an exception that it can't log
     */
    public <T> void log(Exception exception, HystrixCommand<T> commandInstance) {
        checkState(canHandleException(exception));

        logException((E) exception, commandInstance);
    }

    /**
     * @param exception the exception that you should log
     * @param commandInstance you get access to the command that failed, so you can specify what kind it was
     */
    protected abstract <T> void logException(E exception, HystrixCommand<T> commandInstance);
}
