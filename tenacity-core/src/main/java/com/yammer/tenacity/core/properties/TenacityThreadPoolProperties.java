package com.yammer.tenacity.core.properties;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;

public class TenacityThreadPoolProperties extends HystrixThreadPoolProperties {
    protected TenacityThreadPoolProperties(HystrixThreadPoolKey key) {
        super(key);
    }

    protected TenacityThreadPoolProperties(HystrixThreadPoolKey key, Setter builder) {
        super(key, builder);
    }

    protected TenacityThreadPoolProperties(HystrixThreadPoolKey key, Setter builder, String propertyPrefix) {
        super(key, builder, propertyPrefix);
    }

    public static Setter build(TenacityConfiguration configuration) {
        final ThreadPoolConfiguration threadPoolConfiguration = configuration.getThreadpool();
        return Setter()
                .withCoreSize(threadPoolConfiguration.getThreadPoolCoreSize())
                .withKeepAliveTimeMinutes(threadPoolConfiguration.getKeepAliveTimeMinutes())
                .withMaxQueueSize(threadPoolConfiguration.getMaxQueueSize())
                .withQueueSizeRejectionThreshold(threadPoolConfiguration.getQueueSizeRejectionThreshold())
                .withMetricsRollingStatisticalWindowInMilliseconds(threadPoolConfiguration.getMetricsRollingStatisticalWindowInMilliseconds())
                .withMetricsRollingStatisticalWindowBuckets(threadPoolConfiguration.getMetricsRollingStatisticalWindowBuckets());
    }
}