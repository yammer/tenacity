package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.tenacity.core.TenacityPropertyStore;
import com.yammer.tenacity.core.TenacityPropertyStoreBuilder;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import com.yammer.tenacity.core.properties.TenacityCommandProperties;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityThreadPoolProperties;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TenacityPropertiesTest extends TenacityTest {
    private TenacityPropertyStore tenacityPropertyStore;

    @Before
    public void setup() {
        tenacityPropertyStore = new TenacityPropertyStore();
    }

    @Test
    public void executeCorrectly() throws Exception {
        assertThat(new TenacitySuccessCommand(tenacityPropertyStore).execute()).isEqualTo("value");
        assertThat(new TenacitySuccessCommand(tenacityPropertyStore).queue().get()).isEqualTo("value");
    }

    @Test
    public void fallbackCorrectly() throws Exception {
        assertThat(new TenacityFailingCommand(tenacityPropertyStore).execute()).isEqualTo("fallback");
        assertThat(new TenacityFailingCommand(tenacityPropertyStore).queue().get()).isEqualTo("fallback");
    }

    private static class OverridePropertiesBuilder extends TenacityPropertyStoreBuilder<OverrideConfiguration> {
        private OverridePropertiesBuilder(OverrideConfiguration configuration) {
            super(configuration);
        }

        @Override
        public ImmutableMap<TenacityPropertyKey, HystrixCommandProperties.Setter> buildCommandProperties() {

            return ImmutableMap.<TenacityPropertyKey, TenacityCommandProperties.Setter>of(
                    DependencyKey.OVERRIDE, TenacityCommandProperties.build(configuration.getTenacityConfiguration()));
        }

        @Override
        public ImmutableMap<TenacityPropertyKey, HystrixThreadPoolProperties.Setter> buildThreadpoolProperties() {
            return ImmutableMap.<TenacityPropertyKey, TenacityThreadPoolProperties.Setter>of(
                    DependencyKey.OVERRIDE, TenacityThreadPoolProperties.build(configuration.getTenacityConfiguration()));
        }
    }

    private static class OverrideConfiguration extends Configuration {
        private final TenacityConfiguration tenacityConfiguration;

        private OverrideConfiguration(TenacityConfiguration tenacityConfiguration) {
            this.tenacityConfiguration = tenacityConfiguration;
        }

        private TenacityConfiguration getTenacityConfiguration() {
            return tenacityConfiguration;
        }
    }

    @Test
    public void testOverriddenProperties() throws Exception {
        final OverrideConfiguration exampleConfiguration = new OverrideConfiguration(
                new TenacityConfiguration(
                new ThreadPoolConfiguration(50, 3, 27, 57, 2000, 20),
                new CircuitBreakerConfiguration(1, 2, 3),
                982));
        tenacityPropertyStore = new TenacityPropertyStore(new OverridePropertiesBuilder(exampleConfiguration));

        assertThat(new TenacitySuccessCommand(tenacityPropertyStore, DependencyKey.OVERRIDE).execute()).isEqualTo("value");
        assertThat(new TenacitySuccessCommand(tenacityPropertyStore, DependencyKey.OVERRIDE).queue().get()).isEqualTo("value");

        final HystrixThreadPoolProperties threadPoolProperties = HystrixPropertiesFactory
                .getThreadPoolProperties(HystrixThreadPoolKey.Factory.asKey(DependencyKey.OVERRIDE.toString()), null);

        final ThreadPoolConfiguration threadPoolConfiguration = exampleConfiguration.getTenacityConfiguration().getThreadpool();
        assertEquals(threadPoolProperties.coreSize().get().intValue(), threadPoolConfiguration.getThreadPoolCoreSize());
        assertEquals(threadPoolProperties.keepAliveTimeMinutes().get().intValue(), threadPoolConfiguration.getKeepAliveTimeMinutes());
        assertEquals(threadPoolProperties.maxQueueSize().get().intValue(), threadPoolConfiguration.getMaxQueueSize());
        assertEquals(threadPoolProperties.metricsRollingStatisticalWindowBuckets().get().intValue(), threadPoolConfiguration.getMetricsRollingStatisticalWindowBuckets());
        assertEquals(threadPoolProperties.metricsRollingStatisticalWindowInMilliseconds().get().intValue(), threadPoolConfiguration.getMetricsRollingStatisticalWindowInMilliseconds());
        assertEquals(threadPoolProperties.queueSizeRejectionThreshold().get().intValue(), threadPoolConfiguration.getQueueSizeRejectionThreshold());
    }
}
