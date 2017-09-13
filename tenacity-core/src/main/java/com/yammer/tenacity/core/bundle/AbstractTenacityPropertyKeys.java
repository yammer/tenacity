package com.yammer.tenacity.core.bundle;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTenacityPropertyKeys {
    protected final Iterable<TenacityPropertyKey> keys;
    protected final TenacityPropertyKeyFactory keyFactory;
    protected final Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers;
    protected final Optional<HystrixCommandExecutionHook> executionHook;

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        Iterable<TenacityPropertyKey> keys) {
        this(keyFactory, keys, ImmutableList.of(), Optional.empty());
    }

    public AbstractTenacityPropertyKeys(TenacityPropertyKeyFactory keyFactory,
                                        Iterable<TenacityPropertyKey> keys,
                                        Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers,
                                        Optional<HystrixCommandExecutionHook> executionHook) {
        this.keys = ImmutableList.copyOf(checkNotNull(keys));
        this.keyFactory = checkNotNull(keyFactory);
        this.exceptionMappers = ImmutableList.copyOf(checkNotNull(exceptionMappers));
        this.executionHook = executionHook;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(keys, keyFactory, exceptionMappers, executionHook);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractTenacityPropertyKeys other = (AbstractTenacityPropertyKeys) obj;
        return Objects.equal(this.keys, other.keys) &&
                Objects.equal(this.keyFactory, other.keyFactory) &&
                Objects.equal(this.exceptionMappers, other.exceptionMappers) &&
                Objects.equal(this.executionHook, other.executionHook);
    }
}