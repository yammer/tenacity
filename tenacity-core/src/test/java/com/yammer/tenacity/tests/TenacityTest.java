package com.yammer.tenacity.tests;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.tenacity.core.bundle.TenacityBundle;
import org.junit.After;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public abstract class TenacityTest {
    static {
        initialization();
    }

    @SuppressWarnings("unchecked")
    private static void initialization() {
        new TenacityBundle().initialize(new Bootstrap<Configuration>(mock(Service.class)));
        ConfigurationManager
                .getConfigInstance()
                .setProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", "1");
    }

    @After
    public void testTeardown() {
        Hystrix.reset(1, TimeUnit.SECONDS);
    }
}