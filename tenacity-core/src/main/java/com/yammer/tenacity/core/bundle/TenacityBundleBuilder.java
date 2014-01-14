package com.yammer.tenacity.core.bundle;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import java.util.Iterator;

public class TenacityBundleBuilder {
    private Optional<TenacityPropertyKeyFactory> propertyKeyFactory = Optional.absent();
    private Optional<Iterable<TenacityPropertyKey>> propertyKeys = Optional.absent();
    private TenacityExceptionMapper exceptionMapper = new TenacityExceptionMapper(500);

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

    public TenacityBundleBuilder propertyKeys(Iterator<TenacityPropertyKey> keys) {
        this.propertyKeys = Optional.<Iterable<TenacityPropertyKey>>of(ImmutableList.copyOf(keys));
        return this;
    }

    public TenacityBundleBuilder propertyKeys(TenacityPropertyKey... keys) {
        this.propertyKeys = Optional.<Iterable<TenacityPropertyKey>>of(ImmutableList.copyOf(keys));
        return this;
    }

    public TenacityBundleBuilder exceptionMapper(TenacityExceptionMapper tenacityExceptionMapper) {
        exceptionMapper = tenacityExceptionMapper;
        return this;
    }

    public TenacityBundle build() {
        if (!propertyKeyFactory.isPresent()) {
            throw new IllegalArgumentException("Must supply a TenacityPropertyKeyFactory.");
        }

        if (!propertyKeys.isPresent()) {
            throw new IllegalArgumentException("Must supply TenacityPropertyKeys.");
        }

        return new TenacityBundle(propertyKeyFactory.get(), propertyKeys.get(), exceptionMapper);
    }
}
