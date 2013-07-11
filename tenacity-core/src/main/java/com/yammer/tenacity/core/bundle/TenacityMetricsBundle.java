package com.yammer.tenacity.core.bundle;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class TenacityMetricsBundle implements Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(Environment environment) {
        environment.addServlet(new HystrixMetricsStreamServlet(), "/tenacity/metrics.stream");
    }
}