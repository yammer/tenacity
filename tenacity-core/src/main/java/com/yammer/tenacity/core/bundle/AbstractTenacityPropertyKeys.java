package com.yammer.tenacity.core.bundle;

import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTenacityPropertyKeys {
    protected final Iterable<TenacityPropertyKey> keys;
    protected final TenacityPropertyKeyFactory keyFactory;

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        Iterable<TenacityPropertyKey> keys) {
        this.keys = ImmutableList.copyOf(checkNotNull(keys));
        this.keyFactory = checkNotNull(keyFactory);
    }

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        Iterator<TenacityPropertyKey> keys) {
        this(keyFactory, ImmutableList.copyOf(checkNotNull(keys)));
    }

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        TenacityPropertyKey... keys) {
        this(keyFactory, ImmutableList.copyOf(keys));
    }
}