package com.yammer.tenacity.testing;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import org.junit.After;

import java.util.concurrent.TimeUnit;

public abstract class TenacityTest {
    static {
        initialization();
    }

    private static void initialization() {
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
        ConfigurationManager
                .getConfigInstance()
                .setProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", "1");
    }

    @After
    public void testTeardown() {
        Hystrix.reset(1, TimeUnit.SECONDS);
    }
}