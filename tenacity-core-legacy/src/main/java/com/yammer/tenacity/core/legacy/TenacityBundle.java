package com.yammer.tenacity.core.legacy;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;

public class TenacityBundle implements Bundle {
    @Override
    public void initialize(Environment environment) {
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
        environment.addServlet(new HystrixMetricsStreamServlet(), "/tenacity/metrics.stream");
    }
}