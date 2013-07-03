package com.yammer.tenacity.core.properties;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;

public class TenacityCommandProperties extends HystrixCommandProperties {
    public TenacityCommandProperties(HystrixCommandKey key) {
        super(key);
    }

    public TenacityCommandProperties(HystrixCommandKey key, Setter builder) {
        super(key, builder);
    }

    public TenacityCommandProperties(HystrixCommandKey key, Setter builder, String propertyPrefix) {
        super(key, builder, propertyPrefix);
    }

    public static Setter build(TenacityConfiguration configuration) {
        final CircuitBreakerConfiguration circuitBreaker = configuration.getCircuitBreaker();
        return Setter()
                .withCircuitBreakerErrorThresholdPercentage(circuitBreaker.getErrorThresholdPercentage())
                .withCircuitBreakerRequestVolumeThreshold(circuitBreaker.getRequestVolumeThreshold())
                .withCircuitBreakerSleepWindowInMilliseconds(circuitBreaker.getSleepWindowInMillis())
                .withExecutionIsolationThreadTimeoutInMilliseconds(configuration.getExecutionIsolationThreadTimeoutInMillis());
    }
}
