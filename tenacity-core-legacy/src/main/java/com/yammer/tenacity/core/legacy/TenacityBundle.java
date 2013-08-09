package com.yammer.tenacity.core.legacy;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.core.bundle.AbstractTenacityPropertyKeys;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;

import java.util.Iterator;

public class TenacityBundle extends AbstractTenacityPropertyKeys implements Bundle {
    public TenacityBundle(Iterable<TenacityPropertyKey> keys) {
        super(keys);
    }

    public TenacityBundle(Iterator<TenacityPropertyKey> keys) {
        super(keys);
    }

    public TenacityBundle(TenacityPropertyKey... keys) {
        super(keys);
    }

    @Override
    public void initialize(Environment environment) {
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
        environment.addServlet(new HystrixMetricsStreamServlet(), "/tenacity/metrics.stream");
        environment.addResource(new TenacityPropertyKeysResource(keys));
    }
}