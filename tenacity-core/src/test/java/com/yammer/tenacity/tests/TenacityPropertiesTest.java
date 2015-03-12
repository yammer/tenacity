package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import com.yammer.tenacity.core.properties.ArchaiusPropertyRegister;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.core.properties.TenacityPropertyStore;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TenacityPropertiesTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void executeCorrectly() throws Exception {
        assertThat(new TenacitySuccessCommand().execute()).isEqualTo("value");
        assertThat(new TenacitySuccessCommand().queue().get()).isEqualTo("value");
    }

    @Test
    public void fallbackCorrectly() throws Exception {
        assertThat(new TenacityFailingCommand().execute()).isEqualTo("fallback");
        assertThat(new TenacityFailingCommand().queue().get()).isEqualTo("fallback");
    }

    @Test
    public void overrideThreadIsolationTimeout() {
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.THREAD_ISOLATION_TIMEOUT.execution.isolation.thread.timeoutInMilliseconds", "987");
        class ThreadIsolationCommand extends TenacityCommand<String> {
            private ThreadIsolationCommand() {
                super(DependencyKey.THREAD_ISOLATION_TIMEOUT);
            }

            @Override
            protected String run() throws Exception {
                return "value";
            }

            @Override
            protected String getFallback() {
                return "fallback";
            }
        }

        final ThreadIsolationCommand threadIsolationCommand = new ThreadIsolationCommand();

        assertThat(threadIsolationCommand.execute()).isEqualTo("value");

        final HystrixCommandProperties commandProperties = threadIsolationCommand.getCommandProperties();
        assertThat(commandProperties.executionIsolationThreadTimeoutInMilliseconds().get().intValue()).isEqualTo(987);
    }

    @Test
    public void overriddenProperties() throws Exception {
        final TenacityConfiguration overrideConfiguration = new TenacityConfiguration(
                new ThreadPoolConfiguration(50, 3, 27, 57, 2000, 20),
                new CircuitBreakerConfiguration(1, 2, 3, 2000, 20),
                982);

        final TenacityPropertyRegister tenacityPropertyRegister = new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.OVERRIDE, overrideConfiguration),
                new BreakerboxConfiguration(),
                mock(ArchaiusPropertyRegister.class));

        tenacityPropertyRegister.register();

        assertThat(new TenacitySuccessCommand(DependencyKey.OVERRIDE).execute()).isEqualTo("value");
        assertThat(new TenacitySuccessCommand(DependencyKey.OVERRIDE).queue().get()).isEqualTo("value");

        final TenacitySuccessCommand successCommand = new TenacitySuccessCommand(DependencyKey.OVERRIDE);

        assertNotEquals(new TenacitySuccessCommand().getCommandProperties().executionIsolationThreadTimeoutInMilliseconds(), overrideConfiguration.getExecutionIsolationThreadTimeoutInMillis());

        final HystrixCommandProperties commandProperties = successCommand.getCommandProperties();
        assertEquals(commandProperties.executionIsolationThreadTimeoutInMilliseconds().get().intValue(), overrideConfiguration.getExecutionIsolationThreadTimeoutInMillis());
        assertEquals(commandProperties.circuitBreakerErrorThresholdPercentage().get().intValue(), overrideConfiguration.getCircuitBreaker().getErrorThresholdPercentage());
        assertEquals(commandProperties.circuitBreakerRequestVolumeThreshold().get().intValue(), overrideConfiguration.getCircuitBreaker().getRequestVolumeThreshold());
        assertEquals(commandProperties.circuitBreakerSleepWindowInMilliseconds().get().intValue(), overrideConfiguration.getCircuitBreaker().getSleepWindowInMillis());
        assertEquals(commandProperties.metricsRollingStatisticalWindowBuckets().get().intValue(), overrideConfiguration.getCircuitBreaker().getMetricsRollingStatisticalWindowBuckets());
        assertEquals(commandProperties.metricsRollingStatisticalWindowInMilliseconds().get().intValue(), overrideConfiguration.getCircuitBreaker().getMetricsRollingStatisticalWindowInMilliseconds());


        final HystrixThreadPoolProperties threadPoolProperties = successCommand.getThreadpoolProperties();
        final ThreadPoolConfiguration threadPoolConfiguration = overrideConfiguration.getThreadpool();
        assertEquals(threadPoolProperties.coreSize().get().intValue(), threadPoolConfiguration.getThreadPoolCoreSize());
        assertEquals(threadPoolProperties.keepAliveTimeMinutes().get().intValue(), threadPoolConfiguration.getKeepAliveTimeMinutes());
        assertEquals(threadPoolProperties.maxQueueSize().get().intValue(), threadPoolConfiguration.getMaxQueueSize());
        assertEquals(threadPoolProperties.metricsRollingStatisticalWindowBuckets().get().intValue(), threadPoolConfiguration.getMetricsRollingStatisticalWindowBuckets());
        assertEquals(threadPoolProperties.metricsRollingStatisticalWindowInMilliseconds().get().intValue(), threadPoolConfiguration.getMetricsRollingStatisticalWindowInMilliseconds());
        assertEquals(threadPoolProperties.queueSizeRejectionThreshold().get().intValue(), threadPoolConfiguration.getQueueSizeRejectionThreshold());

        assertEquals(TenacityPropertyStore.getTenacityConfiguration(DependencyKey.OVERRIDE), overrideConfiguration);
    }


    private static class SleepCommand extends TenacityCommand<Optional<String>> {
        private SleepCommand(TenacityPropertyKey tenacityPropertyKey) {
            super(tenacityPropertyKey);
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
        final TenacityConfiguration exampleConfiguration = new TenacityConfiguration(
                new ThreadPoolConfiguration(1, 1, 10, queueMaxSize, 10000, 10),
                new CircuitBreakerConfiguration(20, 5000, 50, 10000, 10),
                5000);

        final TenacityPropertyRegister tenacityPropertyRegister = new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.SLEEP, exampleConfiguration),
                new BreakerboxConfiguration(),
                mock(ArchaiusPropertyRegister.class));

        tenacityPropertyRegister.register();

        final ImmutableList.Builder<Future<Optional<String>>> sleepCommands = ImmutableList.builder();
        for (int i = 0; i < queueMaxSize * 2; i++) {
            sleepCommands.add(new SleepCommand(DependencyKey.SLEEP).queue());
        }

        for (Future<Optional<String>> future : sleepCommands.build()) {
            assertFalse(future.isCancelled());
            final Optional<String> result = future.get();
            if (result.isPresent()) {
                assertThat(result).contains("sleep");
            } else {
                assertThat(result).isAbsent();
            }
        }

        final HystrixCommandMetrics sleepCommandMetrics = new SleepCommand(DependencyKey.SLEEP).getCommandMetrics();
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.THREAD_POOL_REJECTED))
                .isEqualTo(4L);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT))
                .isEqualTo(0L);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isEqualTo(4L);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED))
                .isEqualTo(0L);

        final HystrixThreadPoolProperties threadPoolProperties = new SleepCommand(DependencyKey.SLEEP).getThreadpoolProperties();

        final ThreadPoolConfiguration threadPoolConfiguration = exampleConfiguration.getThreadpool();
        assertEquals(threadPoolProperties.queueSizeRejectionThreshold().get().intValue(), threadPoolConfiguration.getQueueSizeRejectionThreshold());
        assertEquals(threadPoolProperties.maxQueueSize().get().intValue(), threadPoolConfiguration.getMaxQueueSize());

        assertEquals(TenacityPropertyStore.getTenacityConfiguration(DependencyKey.SLEEP), exampleConfiguration);
    }

    @Test
    public void queueRejectionWithSynchronousQueue() throws Exception {
        final ImmutableCollection.Builder<Future<Optional<String>>> futures = ImmutableList.builder();
        for (int i = 0; i < 50; i++) {
            futures.add(new SleepCommand(DependencyKey.EXAMPLE).queue());
        }

        for (Future<Optional<String>> future : futures.build()) {
            assertFalse(future.isCancelled());
            final Optional<String> result = future.get();
            if (result.isPresent()) {
                assertThat(result).contains("sleep");
            } else {
                assertThat(result).isAbsent();
            }
        }

        final HystrixCommandMetrics sleepCommandMetrics = new SleepCommand(DependencyKey.EXAMPLE).getCommandMetrics();
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.THREAD_POOL_REJECTED))
                .isGreaterThan(15L);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT))
                .isEqualTo(0L);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isGreaterThanOrEqualTo(40L);
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_FAILURE))
                .isEqualTo(0L);
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_REJECTION))
                .isEqualTo(0L);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED))
                .isEqualTo(sleepCommandMetrics.getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS) -
                        sleepCommandMetrics.getCumulativeCount(HystrixRollingNumberEvent.THREAD_POOL_REJECTED));

        final HystrixThreadPoolProperties threadPoolProperties = new SleepCommand(DependencyKey.EXAMPLE).getThreadpoolProperties();

        //-1 means no limit on the number of items in the queue, which uses the SynchronousBlockingQueue
        assertEquals(threadPoolProperties.maxQueueSize().get().intValue(), -1);
        assertEquals(TenacityPropertyStore.getTenacityConfiguration(DependencyKey.EXAMPLE), new TenacityConfiguration());
    }
}