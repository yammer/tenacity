package com.yammer.tenacity.core.bundle;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.Iterator;

public class TenacityBundleBuilder {
    private Optional<TenacityPropertyKeyFactory> propertyKeyFactory = Optional.absent();
    private Optional<Iterable<TenacityPropertyKey>> propertyKeys = Optional.absent();
    private final ImmutableList.Builder<ExceptionMapper<? extends Throwable>> exceptionMapperBuilder = ImmutableList.builder();
    private Optional<HystrixCommandExecutionHook> executionHook = Optional.absent();

    public static TenacityBundleBuilder newBuilder() {
        return new TenacityBundleBuilder();
    }

    public TenacityBundleBuilder propertyKeyFactory(TenacityPropertyKeyFactory tenacityPropertyKeyFactory) {
        propertyKeyFactory = Optional.fromNullable(tenacityPropertyKeyFactory);
        return this;
    }

    public TenacityBundleBuilder propertyKeys(Iterable<TenacityPropertyKey> keys) {
        propertyKeys = Optional.<Iterable<TenacityPropertyKey>>of(ImmutableList.copyOf(keys));
        return this;
    }

    @SuppressWarnings("unused")
    public TenacityBundleBuilder propertyKeys(Iterator<TenacityPropertyKey> keys) {
        this.propertyKeys = Optional.<Iterable<TenacityPropertyKey>>of(ImmutableList.copyOf(keys));
        return this;
    }

    @SuppressWarnings("unused")
    public TenacityBundleBuilder propertyKeys(TenacityPropertyKey... keys) {
        this.propertyKeys = Optional.<Iterable<TenacityPropertyKey>>of(ImmutableList.copyOf(keys));
        return this;
    }

    public <T extends Throwable> TenacityBundleBuilder addExceptionMapper(ExceptionMapper<T> exceptionMapper) {
        exceptionMapperBuilder.add(exceptionMapper);
        return this;
    }

    public TenacityBundleBuilder mapAllHystrixRuntimeExceptionsTo(int statusCode) {
        exceptionMapperBuilder.add(new TenacityExceptionMapper(statusCode));
        exceptionMapperBuilder.add(new TenacityContainerExceptionMapper(statusCode));
        return this;
    }

    public TenacityBundleBuilder commandExecutionHook(HystrixCommandExecutionHook executionHook) {
        this.executionHook = Optional.of(executionHook);
        return this;
    }

    public TenacityBundle build() {
        if (!propertyKeyFactory.isPresent()) {
            throw new IllegalArgumentException("Must supply a TenacityPropertyKeyFactory.");
        }

        if (!propertyKeys.isPresent()) {
            throw new IllegalArgumentException("Must supply TenacityPropertyKeys.");
        }

        return new TenacityBundle(propertyKeyFactory.get(), propertyKeys.get(), exceptionMapperBuilder.build(), executionHook);
    }
}
