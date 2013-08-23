package com.yammer.tenacity.core.properties;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;

public class TenacityPropertyStore {
    private TenacityPropertyStore() {}

    public static TenacityConfiguration getTenacityConfiguration(TenacityPropertyKey key) {
        final HystrixCommandProperties commandProperties = TenacityCommand.getCommandProperties(key);
        final HystrixThreadPoolProperties threadPoolProperties = TenacityCommand.getThreadpoolProperties(key);
        return new TenacityConfiguration(
                new ThreadPoolConfiguration(
                        threadPoolProperties.coreSize().get(),
                        threadPoolProperties.keepAliveTimeMinutes().get(),
                        threadPoolProperties.maxQueueSize().get(),
                        threadPoolProperties.queueSizeRejectionThreshold().get(),
                        threadPoolProperties.metricsRollingStatisticalWindowInMilliseconds().get(),
                        threadPoolProperties.metricsRollingStatisticalWindowBuckets().get()),
                new CircuitBreakerConfiguration(
                    commandProperties.circuitBreakerRequestVolumeThreshold().get(),
                    commandProperties.circuitBreakerSleepWindowInMilliseconds().get(),
                    commandProperties.circuitBreakerErrorThresholdPercentage().get(),
                    commandProperties.metricsRollingStatisticalWindowInMilliseconds().get(),
                    commandProperties.metricsRollingStatisticalWindowBuckets().get()),
                commandProperties.executionIsolationThreadTimeoutInMilliseconds().get());
    }
}
