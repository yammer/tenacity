package com.yammer.tenacity.client.tests;

import com.google.common.collect.ImmutableMap;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.bundle.TenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Map;

public class TenacityServletService extends Application<Configuration> {
    public static void main(String[] args) throws Exception {
        new TenacityServletService().run(args);
    }

    protected TenacityBundleBuilder<Configuration> tenacityBundleBuilder() {
        return TenacityBundleBuilder
                .newBuilder()
                .configurationFactory(new TenacityBundleConfigurationFactory<Configuration>() {
                    @Override
                    public Map<TenacityPropertyKey, TenacityConfiguration> getTenacityConfigurations(Configuration applicationConfiguration) {
                        final ImmutableMap.Builder<TenacityPropertyKey, TenacityConfiguration> builder = ImmutableMap.builder();
                        for (TenacityPropertyKey key : ServletKeys.values()) {
                            builder.put(key, new TenacityConfiguration());
                        }
                        return builder.build();
                    }

                    @Override
                    public TenacityPropertyKeyFactory getTenacityPropertyKeyFactory(Configuration applicationConfiguration) {
                        return new TenacityPropertyKeyFactory() {
                            @Override
                            public TenacityPropertyKey from(String value) {
                                return ServletKeys.valueOf(value.toUpperCase());
                            }
                        };
                    }

                    @Override
                    public BreakerboxConfiguration getBreakerboxConfiguration(Configuration applicationConfiguration) {
                        return new BreakerboxConfiguration();
                    }
                });
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(tenacityBundleBuilder().build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
