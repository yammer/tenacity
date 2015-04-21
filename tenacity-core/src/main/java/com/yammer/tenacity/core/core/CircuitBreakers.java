package com.yammer.tenacity.core.core;

import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.util.Collection;

public class CircuitBreakers {
    private CircuitBreakers() {}

    public static Collection<CircuitBreaker> allOpen(TenacityPropertyKey... keys) {
        return allOpen(ImmutableList.copyOf(keys));
    }

    public static Collection<CircuitBreaker> allOpen(Iterable<TenacityPropertyKey> keys) {
        final ImmutableList.Builder<CircuitBreaker> builder = ImmutableList.builder();
        for (TenacityPropertyKey key : keys) {
            final HystrixCircuitBreaker circuitBreaker = TenacityCommand.getCircuitBreaker(key);
            if (circuitBreaker != null && !circuitBreaker.allowRequest()) {
                builder.add(new CircuitBreaker(key, true));
            }
        }
        return builder.build();
    }

    public static Collection<CircuitBreaker> all(Iterable<TenacityPropertyKey> keys) {
        final ImmutableList.Builder<CircuitBreaker> circuitBreakerBuilder = ImmutableList.builder();
        for (TenacityPropertyKey key : keys) {
            final HystrixCircuitBreaker circuitBreaker = TenacityCommand.getCircuitBreaker(key);
            if (circuitBreaker != null) {
                circuitBreakerBuilder.add(new CircuitBreaker(key, !circuitBreaker.allowRequest()));
            }
        }
        return circuitBreakerBuilder.build();
    }

    public static Collection<CircuitBreaker> all(TenacityPropertyKey... keys) {
        return all(ImmutableList.copyOf(keys));
    }
}