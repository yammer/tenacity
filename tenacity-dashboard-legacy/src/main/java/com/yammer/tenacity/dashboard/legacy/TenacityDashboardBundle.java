package com.yammer.tenacity.dashboard.legacy;

import com.netflix.hystrix.dashboard.stream.MockStreamServlet;
import com.netflix.hystrix.dashboard.stream.ProxyStreamServlet;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.dashboard.resources.IndexResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenacityDashboardBundle implements ConfiguredBundle<Configuration> {
    @Override
    public void initialize(Configuration configuration, Environment environment) {
        gzipWarning();

        environment.addServlet(new MockStreamServlet(), "/tenacity/mock.stream");
        environment.addServlet(new ProxyStreamServlet(), "/tenacity/proxy.stream");

        environment.addResource(new IndexResource());
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityDashboardBundle.class);

    private static void gzipWarning() {
        LOGGER.warn("\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!    GZIP NEEDS TO BE DISABLED FOR THE TENACITY DASHBOARD                         !\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
}