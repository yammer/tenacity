package com.yammer.tenacity.core.config;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TenacityConfiguration {
    @NotNull @Valid
    private ThreadPoolConfiguration threadpool = new ThreadPoolConfiguration();

    @NotNull @Valid
    private CircuitBreakerConfiguration circuitBreaker = new CircuitBreakerConfiguration();

    @Min(value = 0)
    @Max(Integer.MAX_VALUE)
    private int executionIsolationThreadTimeoutInMillis = 1000;

    public TenacityConfiguration() {}

    //TODO: leverage defaults here for json serialization
    public TenacityConfiguration(ThreadPoolConfiguration threadpool,
                                 CircuitBreakerConfiguration circuitBreaker,
                                 int executionIsolationThreadTimeoutInMillis) {
        this.threadpool = threadpool;
        this.circuitBreaker = circuitBreaker;
        this.executionIsolationThreadTimeoutInMillis = executionIsolationThreadTimeoutInMillis;
    }

    public ThreadPoolConfiguration getThreadpool() {
        return threadpool;
    }

    public CircuitBreakerConfiguration getCircuitBreaker() {
        return circuitBreaker;
    }

    public int getExecutionIsolationThreadTimeoutInMillis() {
        return executionIsolationThreadTimeoutInMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenacityConfiguration that = (TenacityConfiguration) o;

        if (executionIsolationThreadTimeoutInMillis != that.executionIsolationThreadTimeoutInMillis) return false;
        if (!circuitBreaker.equals(that.circuitBreaker)) return false;
        if (!threadpool.equals(that.threadpool)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = threadpool.hashCode();
        result = 31 * result + circuitBreaker.hashCode();
        result = 31 * result + executionIsolationThreadTimeoutInMillis;
        return result;
    }
}
