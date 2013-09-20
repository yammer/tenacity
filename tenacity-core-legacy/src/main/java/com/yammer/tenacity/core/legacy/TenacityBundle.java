package com.yammer.tenacity.core.legacy;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.core.bundle.AbstractTenacityPropertyKeys;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.resources.*;
import com.yammer.tenacity.core.strategies.ManagedConcurrencyStrategy;

import java.util.Iterator;

public class TenacityBundle extends AbstractTenacityPropertyKeys implements Bundle {
    public TenacityBundle(TenacityPropertyKeyFactory keyFactory, Iterable<TenacityPropertyKey> keys) {
        super(keyFactory, keys);
    }

    public TenacityBundle(TenacityPropertyKeyFactory keyFactory, Iterator<TenacityPropertyKey> keys) {
        super(keyFactory, keys);
    }

    public TenacityBundle(TenacityPropertyKeyFactory keyFactory, TenacityPropertyKey... keys) {
        super(keyFactory, keys);
    }

    @Override
    public void initialize(Environment environment) {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new ManagedConcurrencyStrategy(environment));
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
        environment.addServlet(new HystrixMetricsStreamServlet(), "/tenacity/metrics.stream");
        environment.addResource(new TenacityPropertyKeysResource(keys));
        environment.addResource(new TenacityConfigurationResource(keyFactory));
        environment.addResource(new TenacityCircuitBreakersResource(keys));

        //TODO: cgray As of 0.1.6 these are needed to be backwards compatible. These can be removed any future release.
        environment.addResource(new DeprecatedTenacityConfigurationResource(keyFactory));
        environment.addResource(new DeprecatedTenacityPropertyKeysResource(keys));
    }
}