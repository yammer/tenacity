package com.yammer.tenacity.core.helper;

import com.google.common.base.Optional;

import javax.ws.rs.client.ResponseProcessingException;

/**
 * This compound object can be created either with the Result value see {@link #create(Object)}
 * or with a fallback UniformInterfaceException see {@link #clientFailure(javax.ws.rs.client.ResponseProcessingException)}
 * It behaves similar to Google Guava's Optional, throwing an IllegalStateException when
 * an absent value is queried.
 *
 * @param <Result> The generic type of the Object returned when creating a successful instance
 */
public class ClientResponseResult<Result> {

    private final Optional<Result> result;
    private final Optional<ClientException> fallback;
    private final boolean successful;

    private ClientResponseResult(Optional<Result> result, Optional<ClientException> fallbackException, boolean successful) {
        this.result = result;
        this.fallback = fallbackException;
        this.successful = successful;
    }

    /**
     * @param <Result> The expected type of the operation if successful
     * @return A composite object holding only the exception thrown by a client
     */
    public static <Result> ClientResponseResult<Result> clientFailure(ClientException exception) {
        return new ClientResponseResult<>(Optional.<Result>absent(), Optional.of(exception), false);
    }

    /**
     * @param <Result> The expected type of the operation if successful
     * @return A composite object holding only the exception thrown by a client
     */
    public static <Result> ClientResponseResult<Result> clientFailure(ResponseProcessingException ex) {
        return new ClientResponseResult<>(Optional.<Result>absent(), Optional.of(new ClientException(ex)), false);
    }

    /**
     * @param <Result> The expected type of the operation if successful
     * @return A composite object holding only the exception thrown by a client
     */
    public static <Result> ClientResponseResult<Result> clientFailure(String message, int statusCode) {
        return new ClientResponseResult<>(Optional.<Result>absent(), Optional.of(new ClientException(message, statusCode)), false);
    }

    /**
     * @param result   The expected value of a successful operation
     * @param <Result> The expected type of the operation
     * @return A composite object holding only the result value
     */
    public static <Result> ClientResponseResult<Result> create(Result result) {
        return new ClientResponseResult<>(Optional.fromNullable(result), Optional.<ClientException>absent(), true);
    }

    /**
     * @return true if the Result value is present, false if only the Fallback value is available.
     */
    public boolean isSuccess() {
        return successful;
    }

    /**
     * @return The expected Result value on success;
     */
    public Optional<Result> getResult() {
        return result;
    }

    /**
     * @return The expected Fallback value on failure;
     */
    public ClientException getFallbackException() {
        return fallback.get();
    }
}


