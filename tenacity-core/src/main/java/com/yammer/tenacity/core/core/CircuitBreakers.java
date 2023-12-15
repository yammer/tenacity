package com.yammer.tenacity.core.core;

import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CircuitBreakers {
    private CircuitBreakers() {}

    private static Stream<CircuitBreaker> toCircuitBreakers(Collection<TenacityPropertyKey> keys) {
        return keys.stream()
                .map(CircuitBreaker::usingHystrix)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public static Collection<CircuitBreaker> allOpen(TenacityPropertyKey... keys) {
        return allOpen(ImmutableList.copyOf(keys));
    }

    public static Collection<CircuitBreaker> allOpen(Collection<TenacityPropertyKey> keys) {
        return toCircuitBreakers(keys)
                .filter(CircuitBreaker::isOpen)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    public static Collection<CircuitBreaker> all(Collection<TenacityPropertyKey> keys) {
        return toCircuitBreakers(keys)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    public static Collection<CircuitBreaker> all(TenacityPropertyKey... keys) {
        return all(ImmutableList.copyOf(keys));
    }

    public static Optional<CircuitBreaker> find(Collection<TenacityPropertyKey> keys,
                                                TenacityPropertyKey key) {
        return CircuitBreaker.usingHystrix(key.validate(keys));
    }
}