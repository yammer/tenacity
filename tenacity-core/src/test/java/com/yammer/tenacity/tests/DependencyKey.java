package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

public enum DependencyKey implements TenacityPropertyKey, TenacityPropertyKeyFactory {
    EXAMPLE, OVERRIDE, SLEEP, THREAD_ISOLATION_TIMEOUT, NON_EXISTENT_HEALTHCHECK, EXISTENT_HEALTHCHECK, ANOTHER_EXISTENT_HEALTHCHECK,
    TENACITY_AUTH_TIMEOUT, CLIENT_TIMEOUT, OBSERVABLE_TIMEOUT;

    @Override
    public TenacityPropertyKey from(String value) {
        return valueOf(value);
    }
}