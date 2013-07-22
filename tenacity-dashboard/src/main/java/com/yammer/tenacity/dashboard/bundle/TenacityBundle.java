package com.yammer.tenacity.dashboard.bundle;

import com.netflix.hystrix.dashboard.stream.MockStreamServlet;
import com.netflix.hystrix.dashboard.stream.ProxyStreamServlet;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.dashboard.resources.IndexResource;

public class TenacityBundle implements ConfiguredBundle<Configuration> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        configuration
                .getHttpConfiguration()
                .getGzipConfiguration()
                .setEnabled(false);

        environment.addServlet(new MockStreamServlet(), "/tenacity/mock.stream");
        environment.addServlet(new ProxyStreamServlet(), "/tenacity/proxy.stream");

        environment.addResource(new IndexResource());
    }
}