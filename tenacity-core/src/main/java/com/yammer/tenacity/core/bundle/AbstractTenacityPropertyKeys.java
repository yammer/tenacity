package com.yammer.tenacity.core.bundle;

import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import javax.ws.rs.ext.ExceptionMapper;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTenacityPropertyKeys {
    protected final Iterable<TenacityPropertyKey> keys;
    protected final TenacityPropertyKeyFactory keyFactory;
    protected final Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers;

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        Iterable<TenacityPropertyKey> keys) {
        this(keyFactory, keys, ImmutableList.<ExceptionMapper<? extends Throwable>>of());
    }

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        Iterable<TenacityPropertyKey> keys,
                                        Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers) {
        this.keys = ImmutableList.copyOf(checkNotNull(keys));
        this.keyFactory = checkNotNull(keyFactory);
        this.exceptionMappers = ImmutableList.copyOf(checkNotNull(exceptionMappers));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractTenacityPropertyKeys that = (AbstractTenacityPropertyKeys) o;

        if (!exceptionMappers.equals(that.exceptionMappers)) return false;
        if (!keyFactory.equals(that.keyFactory)) return false;
        if (!keys.equals(that.keys)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = keys.hashCode();
        result = 31 * result + keyFactory.hashCode();
        result = 31 * result + exceptionMappers.hashCode();
        return result;
    }
}