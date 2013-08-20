package com.yammer.tenacity.core.bundle;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
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
    public void initialize(Bootstrap<?> bootstrap) {
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
    }

    @Override
    public void run(BreakerboxConfiguration configuration, Environment environment) throws Exception {
        ArchaiusPropertyRegister.register(configuration);
        environment.addServlet(new HystrixMetricsStreamServlet(), "/tenacity/metrics.stream");
        environment.addResource(new TenacityPropertyKeysResource(keys));
    }
}