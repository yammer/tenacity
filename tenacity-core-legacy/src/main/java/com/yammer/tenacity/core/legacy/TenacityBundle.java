package com.yammer.tenacity.core.legacy;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.core.bundle.AbstractTenacityPropertyKeys;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.properties.ArchaiusPropertyRegister;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;

import java.util.Iterator;

public class TenacityBundle extends AbstractTenacityPropertyKeys implements ConfiguredBundle<BreakerboxConfiguration> {
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
    public void initialize(BreakerboxConfiguration configuration, Environment environment) {
        ArchaiusPropertyRegister.register(configuration);
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
        environment.addServlet(new HystrixMetricsStreamServlet(), "/tenacity/metrics.stream");
        environment.addResource(new TenacityPropertyKeysResource(keys));
    }
}