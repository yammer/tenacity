package com.yammer.tenacity.core.legacy.tests;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.legacy.TenacityBundle;
import org.junit.After;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public abstract class TenacityTest {
    static {
        initialization();
    }

    @SuppressWarnings("unchecked")
    private static void initialization() {
        new TenacityBundle().initialize(
                new BreakerboxConfiguration("", 0, 60000),
                new Environment(mock(Service.class), mock(Configuration.class)));
        ConfigurationManager
                .getConfigInstance()
                .setProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", "1");
    }

    @After
    public void testTeardown() {
        Hystrix.reset(1, TimeUnit.SECONDS);
    }
}