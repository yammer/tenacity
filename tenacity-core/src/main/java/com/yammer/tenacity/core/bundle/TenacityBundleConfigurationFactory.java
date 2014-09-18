package com.yammer.tenacity.core.bundle;

import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import io.dropwizard.Configuration;

import java.util.Map;

public interface TenacityBundleConfigurationFactory<T extends Configuration> {

    Map<TenacityPropertyKey, TenacityConfiguration> getTenacityConfigurations(T applicationConfiguration);

    TenacityPropertyKeyFactory getTenacityPropertyKeyFactory(T applicationConfiguration);

    BreakerboxConfiguration getBreakerboxConfiguration(T applicationConfiguration);

}
