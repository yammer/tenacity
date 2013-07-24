package com.yammer.tenacity.dashboard.bundle;

import com.netflix.hystrix.dashboard.stream.MockStreamServlet;
import com.netflix.hystrix.dashboard.stream.ProxyStreamServlet;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;
import com.yammer.tenacity.dashboard.resources.IndexResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenacityDashboardBundle implements ConfiguredBundle<Configuration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityDashboardBundle.class);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle());
    }

    private static void gzipWarning() {
        LOGGER.warn("\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!    GZIP HAS BEEN DISABLED FOR THE TENACITY DASHBOARD                         !\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        configuration
                .getHttpConfiguration()
                .getGzipConfiguration()
                .setEnabled(false);

        gzipWarning();

        environment.addServlet(new MockStreamServlet(), "/tenacity/mock.stream");
        environment.addServlet(new ProxyStreamServlet(), "/tenacity/proxy.stream");

        environment.addResource(new IndexResource());
    }
}