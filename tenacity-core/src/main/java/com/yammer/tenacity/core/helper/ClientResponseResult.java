package com.yammer.tenacity.core.helper;

import com.google.common.base.Optional;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * This compound object can be created either with the Result value see {@link #create(Object)}
 * or with a fallback UniformInterfaceException see {@link #clientFailure(com.sun.jersey.api.client.UniformInterfaceException)}
 * It behaves similar to Google Guava's Optional, throwing an IllegalStateException when
 * an absent value is queried.
 *
 * @param <Result> The generic type of the Object returned when creating a successful instance
 */
public class ClientResponseResult<Result> {

    private final Optional<Result> result;
    private final Optional<UniformInterfaceException> fallback;
    private final boolean successful;

    private ClientResponseResult(Optional<Result> result, Optional<UniformInterfaceException> fallbackException){
        this.result = result;
        this.fallback = fallbackException;
        this.successful = result.isPresent();
    }

    /**
     * @param <Result> The expected type of the operation if successful
     * @return A composite object holding only the exception thrown by a client
     */
    public static <Result> ClientResponseResult<Result> clientFailure(UniformInterfaceException exception){
        return new ClientResponseResult<>(Optional.<Result>absent(),Optional.of(exception));
    }

    /**
     *
     * @param result The expected value of a successful operation
     * @param <Result> The expected type of the operation
     * @param <Fallback> The expected type of the fallback
     * @return A composite object holding only the result value
     */
    public static <Result,Fallback> ClientResponseResult<Result> create(Result result){
        return new ClientResponseResult<>(Optional.fromNullable(result), Optional.<UniformInterfaceException>absent());
    }

    /**
     *
     * @return true if the Result value is present, false if only the Fallback value is available.
     */
    public boolean isSuccess(){
        return successful;
    }

    /**
     *
     * @return The expected Result value on success;
     */
    public Result getResult(){
        return result.get();
    }

    /**
     *
     * @return The expected Fallback value on failure;
     */
    public UniformInterfaceException getFallbackException(){
        return fallback.get();
    }
}


