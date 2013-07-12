package com.yammer.tenacity.dashboard.bundle;

import com.netflix.hystrix.dashboard.stream.MockStreamServlet;
import com.netflix.hystrix.dashboard.stream.ProxyStreamServlet;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.dashboard.resources.IndexResource;

public class TenacityBundle implements Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(Environment environment) {
        environment.addServlet(new MockStreamServlet(), "/tenacity/mock.stream");
        environment.addServlet(new ProxyStreamServlet(), "/tenacity/proxy.stream");

        environment.addResource(new IndexResource());
    }
}