package com.yammer.tenacity.core.core;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
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
            final Optional<CircuitBreaker> circuitBreakerOptional = CircuitBreaker.usingHystrix(key);
            if (circuitBreakerOptional.isPresent()) {
                final CircuitBreaker circuitBreaker = circuitBreakerOptional.get();
                if (circuitBreaker.isOpen()) {
                    builder.add(circuitBreaker);
                }
            }
        }
        return builder.build();
    }

    public static Collection<CircuitBreaker> all(Iterable<TenacityPropertyKey> keys) {
        final ImmutableList.Builder<CircuitBreaker> circuitBreakerBuilder = ImmutableList.builder();
        for (TenacityPropertyKey key : keys) {
            final Optional<CircuitBreaker> circuitBreakerOptional = CircuitBreaker.usingHystrix(key);
            if (circuitBreakerOptional.isPresent()) {
                circuitBreakerBuilder.add(circuitBreakerOptional.get());
            }
        }
        return circuitBreakerBuilder.build();
    }

    public static Collection<CircuitBreaker> all(TenacityPropertyKey... keys) {
        return all(ImmutableList.copyOf(keys));
    }
}