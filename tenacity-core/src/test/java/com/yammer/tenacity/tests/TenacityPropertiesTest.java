package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.TenacityPropertyStore;
import com.yammer.tenacity.core.TenacityPropertyStoreBuilder;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import com.yammer.tenacity.core.metrics.TenacityMetrics;
import com.yammer.tenacity.core.properties.TenacityCommandProperties;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityThreadPoolProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
        private final DependencyKey key;
        private OverridePropertiesBuilder(DependencyKey key,
                                          OverrideConfiguration configuration) {
            super(configuration);
            this.key = key;
        }

        @Override
        public ImmutableMap<TenacityPropertyKey, HystrixCommandProperties.Setter> buildCommandProperties() {

            return ImmutableMap.<TenacityPropertyKey, TenacityCommandProperties.Setter>of(key, TenacityCommandProperties.build(configuration.getTenacityConfiguration()));
        }

        @Override
        public ImmutableMap<TenacityPropertyKey, HystrixThreadPoolProperties.Setter> buildThreadpoolProperties() {
            return ImmutableMap.<TenacityPropertyKey, TenacityThreadPoolProperties.Setter>of(key, TenacityThreadPoolProperties.build(configuration.getTenacityConfiguration()));
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
    public void overriddenProperties() throws Exception {
        final OverrideConfiguration exampleConfiguration = new OverrideConfiguration(
                new TenacityConfiguration(
                new ThreadPoolConfiguration(50, 3, 27, 57, 2000, 20),
                new CircuitBreakerConfiguration(1, 2, 3),
                982));
        tenacityPropertyStore = new TenacityPropertyStore(new OverridePropertiesBuilder(DependencyKey.OVERRIDE, exampleConfiguration));

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

    private static class SleepCommand extends TenacityCommand<Optional<String>> {
        private SleepCommand(String commandGroupKey, String commandKey, TenacityPropertyStore tenacityPropertyStore, TenacityPropertyKey tenacityPropertyKey) {
            super(commandGroupKey, commandKey, tenacityPropertyStore, tenacityPropertyKey);
        }

        @Override
        protected Optional<String> run() throws Exception {
            Thread.sleep(500);
            return Optional.of("sleep");
        }

        @Override
        protected Optional<String> getFallback() {
            return Optional.absent();
        }
    }

    @Test
    public void queueRejectionWithBlockingQueue() throws Exception {
        final int queueMaxSize = 5;
        final OverrideConfiguration exampleConfiguration = new OverrideConfiguration(
                new TenacityConfiguration(
                        new ThreadPoolConfiguration(1, 1, 10, queueMaxSize, 10000, 10),
                        new CircuitBreakerConfiguration(20, 5000, 50),
                        5000));
        tenacityPropertyStore = new TenacityPropertyStore(new OverridePropertiesBuilder(DependencyKey.SLEEP, exampleConfiguration));

        final ImmutableList.Builder<Future<Optional<String>>> sleepCommands = ImmutableList.builder();
        for (int i = 0; i < queueMaxSize * 2; i++) {
            sleepCommands.add(new SleepCommand("queueRejectionWithBlockingQueue", "Sleep", tenacityPropertyStore, DependencyKey.SLEEP).queue());
        }

        for (Future<Optional<String>> future : sleepCommands.build()) {
            assertFalse(future.isCancelled());
            final Optional<String> result = future.get();
            if (result.isPresent()) {
                assertThat(result.get()).isEqualTo("sleep");
            } else {
                assertThat(result).isEqualTo(Optional.<String>absent());
            }
        }

        final HystrixCommandMetrics sleepCommandMetrics = TenacityMetrics.getCommandMetrics(new SleepCommand("queueRejectionWithBlockingQueue", "Sleep", tenacityPropertyStore, DependencyKey.SLEEP));
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.THREAD_POOL_REJECTED))
                .isEqualTo(4);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT))
                .isEqualTo(0);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isEqualTo(4);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED))
                .isEqualTo(0);

        final HystrixThreadPoolProperties threadPoolProperties = HystrixPropertiesFactory
                .getThreadPoolProperties(HystrixThreadPoolKey.Factory.asKey(DependencyKey.SLEEP.toString()), null);

        final ThreadPoolConfiguration threadPoolConfiguration = exampleConfiguration.getTenacityConfiguration().getThreadpool();
        assertEquals(threadPoolProperties.queueSizeRejectionThreshold().get().intValue(), threadPoolConfiguration.getQueueSizeRejectionThreshold());
        assertEquals(threadPoolProperties.maxQueueSize().get().intValue(), threadPoolConfiguration.getMaxQueueSize());
    }

    @Test
    public void queueRejectionWithSynchronousQueue() throws Exception {
        final ImmutableCollection.Builder<Future<Optional<String>>> futures = ImmutableList.builder();
        for (int i = 0; i < 50; i++) {
            futures.add(new SleepCommand("queueRejectionWithSynchronousQueue", "syncQueue",
                    tenacityPropertyStore, DependencyKey.EXAMPLE).queue());
        }

        for (Future<Optional<String>> future : futures.build()) {
            assertFalse(future.isCancelled());
            final Optional<String> result = future.get();
            if (result.isPresent()) {
                assertThat(result.get()).isEqualTo("sleep");
            } else {
                assertThat(result).isEqualTo(Optional.<String>absent());
            }
        }

        final HystrixCommandMetrics sleepCommandMetrics = TenacityMetrics.getCommandMetrics(new SleepCommand("queueRejectionWithSynchronousQueue", "syncQueue",
                tenacityPropertyStore, DependencyKey.EXAMPLE));
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.THREAD_POOL_REJECTED))
                .isEqualTo(40);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT))
                .isEqualTo(0);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isEqualTo(40);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED))
                .isEqualTo(0);

        final HystrixThreadPoolProperties threadPoolProperties = HystrixPropertiesFactory
                .getThreadPoolProperties(HystrixThreadPoolKey.Factory.asKey(DependencyKey.EXAMPLE.toString()), null);

        //-1 means no limit on the number of items in the queue, which uses the SynchronousBlockingQueue
        assertEquals(threadPoolProperties.maxQueueSize().get().intValue(), -1);
    }
}
