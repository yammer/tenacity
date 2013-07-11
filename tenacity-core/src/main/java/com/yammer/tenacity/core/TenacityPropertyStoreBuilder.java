package com.yammer.tenacity.core;

import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.tenacity.core.properties.TenacityCommandProperties;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityThreadPoolProperties;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TenacityPropertyStoreBuilder<ConfigType extends Configuration> {
    protected final ConfigType configuration;

    protected TenacityPropertyStoreBuilder(ConfigType configuration) {
        this.configuration = checkNotNull(configuration);
    }

    public abstract ImmutableMap<TenacityPropertyKey, TenacityCommandProperties.Setter> buildCommandProperties();
    public abstract ImmutableMap<TenacityPropertyKey, TenacityThreadPoolProperties.Setter> buildThreadpoolProperties();
}