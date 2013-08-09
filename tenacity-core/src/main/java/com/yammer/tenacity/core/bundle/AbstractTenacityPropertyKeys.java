package com.yammer.tenacity.core.bundle;

import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.util.Iterator;

public abstract class AbstractTenacityPropertyKeys {
    protected final Iterable<TenacityPropertyKey> keys;

    public AbstractTenacityPropertyKeys(Iterable<TenacityPropertyKey> keys) {
        this.keys = ImmutableList.copyOf(keys);
    }

    public AbstractTenacityPropertyKeys(Iterator<TenacityPropertyKey> keys) {
        this(ImmutableList.copyOf(keys));
    }

    public AbstractTenacityPropertyKeys(TenacityPropertyKey... keys) {
        this(ImmutableList.copyOf(keys));
    }
}