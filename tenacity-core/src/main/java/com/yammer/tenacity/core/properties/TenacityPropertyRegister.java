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

    public TenacityPropertyRegister(Map<TenacityPropertyKey, TenacityConfiguration> configurations,
                                    BreakerboxConfiguration breakerboxConfiguration) {
        this(configurations, breakerboxConfiguration, new ArchaiusPropertyRegister());
    }

    public TenacityPropertyRegister(Map<TenacityPropertyKey, TenacityConfiguration> configurations,
                                    BreakerboxConfiguration breakerboxConfiguration,
                                    ArchaiusPropertyRegister archaiusPropertyRegister) {
        this.configurations = ImmutableMap.copyOf(configurations);
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

    public static void registerCircuitForceOpen(TenacityPropertyKey key) {
        ConfigurationManager.getConfigInstance().setProperty(circuitBreakerForceOpen(key), true);
    }

    public static void registerCircuitForceClosed(TenacityPropertyKey key) {
        ConfigurationManager.getConfigInstance().setProperty(circuitBreakerForceClosed(key), true);
    }

    public static void registerCircuitForceReset(TenacityPropertyKey key) {
        final AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
        configInstance.setProperty(circuitBreakerForceOpen(key), false);
        configInstance.setProperty(circuitBreakerForceClosed(key), false);
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

        configInstance.setProperty(
                semaphoreMaxConcurrentRequests(key),
                configuration.getSemaphore().getMaxConcurrentRequests());

        configInstance.setProperty(
                semaphoreFallbackMaxConcurrentRequests(key),
                configuration.getSemaphore().getFallbackMaxConcurrentRequests());

        if (configuration.hasExecutionIsolationStrategy()) {
            configInstance.setProperty(
                    executionIsolationStrategy(key),
                    configuration.getExecutionIsolationStrategy());
        }
    }

    public static String executionIsolationThreadTimeoutInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.execution.isolation.thread.timeoutInMilliseconds", key.name());
    }

    public static String circuitBreakerRequestVolumeThreshold(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.requestVolumeThreshold", key.name());
    }

    public static String circuitBreakerSleepWindowInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.sleepWindowInMilliseconds", key.name());
    }

    public static String circuitBreakerErrorThresholdPercentage(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.errorThresholdPercentage", key.name());
    }

    public static String circuitBreakermetricsRollingStatsTimeInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.metrics.rollingStats.timeInMilliseconds", key.name());
    }

    public static String circuitBreakermetricsRollingStatsNumBuckets(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.metrics.rollingStats.numBuckets", key.name());
    }

    public static String threadpoolMetricsRollingStatsTimeInMilliseconds(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.metrics.rollingStats.timeInMilliseconds", key.name());
    }

    public static String threadpoolMetricsRollingStatsNumBuckets(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.metrics.rollingStats.numBuckets", key.name());
    }

    public static String threadpoolCoreSize(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.coreSize", key.name());
    }

    public static String threadpoolKeepAliveTimeMinutes(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.keepAliveTimeMinutes", key.name());
    }

    public static String threadpoolQueueSizeRejectionThreshold(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.queueSizeRejectionThreshold", key.name());
    }

    public static String threadpoolMaxQueueSize(TenacityPropertyKey key) {
        return String.format("hystrix.threadpool.%s.maxQueueSize", key.name());
    }

    public static String semaphoreMaxConcurrentRequests(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.execution.isolation.semaphore.maxConcurrentRequests", key.name());
    }

    public static String semaphoreFallbackMaxConcurrentRequests(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.fallback.isolation.semaphore.maxConcurrentRequests", key.name());
    }

    public static String executionIsolationStrategy(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.execution.isolation.strategy", key.name());
    }

    public static String circuitBreakerForceOpen(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.forceOpen", key.name());
    }

    public static String circuitBreakerForceClosed(TenacityPropertyKey key) {
        return String.format("hystrix.command.%s.circuitBreaker.forceClosed", key.name());
    }
}