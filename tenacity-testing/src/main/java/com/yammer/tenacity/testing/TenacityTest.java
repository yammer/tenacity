package com.yammer.tenacity.testing;

import com.codahale.metrics.MetricRegistry;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.TimeUnit;

/**
 * @deprecated use {@link TenacityTestRule} instead
 */
@Deprecated
public abstract class TenacityTest {
    @Before
    public void testInitialization() {
        resetHystrixPlugins();
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixCodaHaleMetricsPublisher(new MetricRegistry()));
        ConfigurationManager
                .getConfigInstance()
                .setProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", "1");
    }

    @After
    public void testTeardown() {
        Hystrix.reset(1, TimeUnit.SECONDS);
        ConfigurationManager.getConfigInstance().clear();
        resetHystrixPlugins();
    }

    private static void resetHystrixPlugins() {
        HystrixPlugins.reset();
    }
}