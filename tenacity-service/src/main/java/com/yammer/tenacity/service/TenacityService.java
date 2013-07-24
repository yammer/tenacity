package com.yammer.tenacity.service;

import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.dashboard.bundle.TenacityDashboardBundle;
import com.yammer.tenacity.service.config.TenacityConfiguration;
import com.yammer.tenacity.service.resources.SelectionResource;

public class TenacityService extends Service<TenacityConfiguration> {
    public static void main(String[] args) throws Exception {
        new TenacityService().run(args);
    }

    private TenacityService() {}

    @Override
    public void initialize(Bootstrap<TenacityConfiguration> bootstrap) {
        bootstrap.setName("Tenacity");
        bootstrap.addBundle(new TenacityDashboardBundle());

        TurbineInit.init();
    }

    @Override
    public void run(TenacityConfiguration configuration, Environment environment) throws Exception {
        environment.addServlet(new TurbineStreamServlet(), "/turbine.stream");

        environment.addResource(new SelectionResource());
    }
}