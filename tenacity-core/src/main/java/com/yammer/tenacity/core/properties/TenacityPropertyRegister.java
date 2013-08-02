package com.yammer.tenacity.core.properties;

import com.google.common.collect.ImmutableMap;
import com.netflix.config.ConfigurationManager;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.apache.commons.configuration.AbstractConfiguration;

import java.util.Map;

public class TenacityPropertyRegister {
    protected final ImmutableMap<TenacityPropertyKey, TenacityConfiguration> configurations;

    public TenacityPropertyRegister(ImmutableMap<TenacityPropertyKey, TenacityConfiguration> configurations) {
        this.configurations = configurations;
    }

    public void register() {
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

    private static String executionIsolationThreadTimeoutInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.execution.isolation.thread.timeoutInMilliseconds", key);
    }

    private static String circuitBreakerRequestVolumeThreshold(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.requestVolumeThreshold", key);
    }

    private static String circuitBreakerSleepWindowInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.sleepWindowInMilliseconds", key);
    }

    private static String circuitBreakerErrorThresholdPercentage(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.errorThresholdPercentage", key);
    }

    private static String circuitBreakermetricsRollingStatsTimeInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.metrics.rollingStats.timeInMilliseconds", key);
    }

    private static String circuitBreakermetricsRollingStatsNumBuckets(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.metrics.rollingStats.numBuckets", key);
    }

    private static String threadpoolMetricsRollingStatsTimeInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.metrics.rollingStats.timeInMilliseconds", key);
    }

    private static String threadpoolMetricsRollingStatsNumBuckets(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.metrics.rollingStats.numBuckets", key);
    }

    private static String threadpoolCoreSize(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.coreSize", key);
    }

    private static String threadpoolKeepAliveTimeMinutes(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.keepAliveTimeMinutes", key);
    }

    private static String threadpoolQueueSizeRejectionThreshold(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.queueSizeRejectionThreshold", key);
    }

    private static String threadpoolMaxQueueSize(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.maxQueueSize", key);
    }
}
