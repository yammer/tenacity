package com.yammer.tenacity.dashboard;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.dashboard.bundle.TenacityDashboardBundle;

public class TenacityDashboardService extends Service<Configuration> {
    private TenacityDashboardService() {}

    public static void main(String[] args) throws Exception {
        new TenacityDashboardService().run(args);
    }
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new TenacityDashboardBundle());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
