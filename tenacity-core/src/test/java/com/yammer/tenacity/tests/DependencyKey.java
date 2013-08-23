package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

public enum DependencyKey implements TenacityPropertyKey, TenacityPropertyKeyFactory {
    EXAMPLE, OVERRIDE, SLEEP, THREAD_ISOLATION_TIMEOUT;

    @Override
    public TenacityPropertyKey from(String value) {
        return valueOf(value);
    }
}