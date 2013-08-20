package com.yammer.tenacity.core.properties;

import com.google.common.collect.ImmutableMap;
import com.netflix.config.ConfigurationManager;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.apache.commons.configuration.AbstractConfiguration;

import java.util.Map;

public class TenacityPropertyRegister {
    protected final ImmutableMap<TenacityPropertyKey, TenacityConfiguration> configurations;
    protected final BreakerboxConfiguration breakerboxConfiguration;
    protected final ArchaiusPropertyRegister archaiusPropertyRegister;

    public TenacityPropertyRegister(ImmutableMap<TenacityPropertyKey, TenacityConfiguration> configurations,
                                    BreakerboxConfiguration breakerboxConfiguration,
                                    ArchaiusPropertyRegister archaiusPropertyRegister) {
        this.configurations = configurations;
        this.breakerboxConfiguration = breakerboxConfiguration;
        this.archaiusPropertyRegister = archaiusPropertyRegister;
    }

    public void register() {
        archaiusPropertyRegister.register(breakerboxConfiguration);
        final AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
        for (Map.Entry<TenacityPropertyKey, TenacityConfiguration> entry : configurations.entrySet()) {
            registerConfiguration(entry.getKey(), entry.getValue(), configInstance);
        }
    }

    private void registerConfiguration(TenacityPropertyKey key,
                                       TenacityConfiguration configuration,
                                       AbstractConfiguration configInstance) {
        configInstance.setProperty(
                executionIsolationThreadTimeoutInMilliseconds(key),
                configuration.getExecutionIsolationThreadTimeoutInMillis());

        configInstance.setProperty(
                circuitBreakerRequestVolumeThreshold(key),
                configuration.getCircuitBreaker().getRequestVolumeThreshold());

        configInstance.setProperty(
                circuitBreakerSleepWindowInMilliseconds(key),
                configuration.getCircuitBreaker().getSleepWindowInMillis());

        configInstance.setProperty(
                circuitBreakerErrorThresholdPercentage(key),
                configuration.getCircuitBreaker().getErrorThresholdPercentage());

        configInstance.setProperty(
                circuitBreakermetricsRollingStatsNumBuckets(key),
                configuration.getCircuitBreaker().getMetricsRollingStatisticalWindowBuckets());

        configInstance.setProperty(
                circuitBreakermetricsRollingStatsTimeInMilliseconds(key),
                configuration.getCircuitBreaker().getMetricsRollingStatisticalWindowInMilliseconds());

        configInstance.setProperty(
                threadpoolCoreSize(key),
                configuration.getThreadpool().getThreadPoolCoreSize());

        configInstance.setProperty(
                threadpoolKeepAliveTimeMinutes(key),
                configuration.getThreadpool().getKeepAliveTimeMinutes());

        configInstance.setProperty(
                threadpoolMaxQueueSize(key),
                configuration.getThreadpool().getMaxQueueSize());

        configInstance.setProperty(
                threadpoolQueueSizeRejectionThreshold(key),
                configuration.getThreadpool().getQueueSizeRejectionThreshold());

        configInstance.setProperty(
                threadpoolMetricsRollingStatsNumBuckets(key),
                configuration.getThreadpool().getMetricsRollingStatisticalWindowBuckets());

        configInstance.setProperty(
                threadpoolMetricsRollingStatsTimeInMilliseconds(key),
                configuration.getThreadpool().getMetricsRollingStatisticalWindowInMilliseconds());
    }

    public static String executionIsolationThreadTimeoutInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.execution.isolation.thread.timeoutInMilliseconds", key);
    }

    public static String circuitBreakerRequestVolumeThreshold(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.requestVolumeThreshold", key);
    }

    public static String circuitBreakerSleepWindowInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.sleepWindowInMilliseconds", key);
    }

    public static String circuitBreakerErrorThresholdPercentage(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.errorThresholdPercentage", key);
    }

    public static String circuitBreakermetricsRollingStatsTimeInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.metrics.rollingStats.timeInMilliseconds", key);
    }

    public static String circuitBreakermetricsRollingStatsNumBuckets(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.metrics.rollingStats.numBuckets", key);
    }

    public static String threadpoolMetricsRollingStatsTimeInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.metrics.rollingStats.timeInMilliseconds", key);
    }

    public static String threadpoolMetricsRollingStatsNumBuckets(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.metrics.rollingStats.numBuckets", key);
    }

    public static String threadpoolCoreSize(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.coreSize", key);
    }

    public static String threadpoolKeepAliveTimeMinutes(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.keepAliveTimeMinutes", key);
    }

    public static String threadpoolQueueSizeRejectionThreshold(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.queueSizeRejectionThreshold", key);
    }

    public static String threadpoolMaxQueueSize(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.maxQueueSize", key);
    }
}
