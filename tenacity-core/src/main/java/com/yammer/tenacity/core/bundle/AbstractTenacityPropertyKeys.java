package com.yammer.tenacity.core.bundle;

import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTenacityPropertyKeys {
    protected final Iterable<TenacityPropertyKey> keys;
    protected final TenacityPropertyKeyFactory keyFactory;
    protected final TenacityExceptionMapper exceptionMapper;

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        Iterable<TenacityPropertyKey> keys) {
        this(keyFactory, keys, new TenacityExceptionMapper(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        Iterable<TenacityPropertyKey> keys,
                                        TenacityExceptionMapper exceptionMapper) {
        this.keys = ImmutableList.copyOf(checkNotNull(keys));
        this.keyFactory = checkNotNull(keyFactory);
        this.exceptionMapper = checkNotNull(exceptionMapper);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractTenacityPropertyKeys that = (AbstractTenacityPropertyKeys) o;

        if (!exceptionMapper.equals(that.exceptionMapper)) return false;
        if (!keyFactory.equals(that.keyFactory)) return false;
        if (!keys.equals(that.keys)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = keys.hashCode();
        result = 31 * result + keyFactory.hashCode();
        result = 31 * result + exceptionMapper.hashCode();
        return result;
    }
}