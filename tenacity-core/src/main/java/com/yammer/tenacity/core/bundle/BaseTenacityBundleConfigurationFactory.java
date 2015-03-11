package com.yammer.tenacity.core.bundle;

import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.Configuration;

import java.util.Collections;
import java.util.Map;

public abstract class BaseTenacityBundleConfigurationFactory<T extends Configuration> implements TenacityBundleConfigurationFactory<T> {

    @Override
    public Map<TenacityPropertyKey, TenacityConfiguration> getTenacityConfigurations(T applicationConfiguration) {
        return Collections.emptyMap();
    }

    @Override
    public BreakerboxConfiguration getBreakerboxConfiguration(T applicationConfiguration) {
        return new BreakerboxConfiguration();
    }
}