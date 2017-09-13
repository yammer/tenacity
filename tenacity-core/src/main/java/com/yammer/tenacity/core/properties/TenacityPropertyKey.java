package com.yammer.tenacity.core.properties;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public interface TenacityPropertyKey extends HystrixCommandKey, HystrixThreadPoolKey {
    default Predicate<TenacityPropertyKey> isEqualPredicate() {
        return (value) -> value.name().equals(name());
    }

    default TenacityPropertyKey validate(Collection<TenacityPropertyKey> keys) {
        return keys.stream()
                .filter(isEqualPredicate())
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("No TenacityPropertyKey " + name()));
    }
}