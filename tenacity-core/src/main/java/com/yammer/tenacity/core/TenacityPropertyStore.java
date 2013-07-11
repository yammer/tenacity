package com.yammer.tenacity.core;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import com.yammer.tenacity.core.properties.TenacityCommandProperties;
import com.yammer.tenacity.core.properties.TenacityHystrixPropertiesStrategy;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityThreadPoolProperties;

import static com.google.common.base.Preconditions.checkNotNull;

public class TenacityPropertyStore {
    static {
        HystrixPlugins.getInstance().registerPropertiesStrategy(new TenacityHystrixPropertiesStrategy());
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
    }

    protected volatile ImmutableMap<TenacityPropertyKey, TenacityCommandProperties.Setter> commandProperties;
    protected volatile ImmutableMap<TenacityPropertyKey, TenacityThreadPoolProperties.Setter> threadpoolProperties;

    public TenacityPropertyStore() {
        this.commandProperties = ImmutableMap.of();
        this.threadpoolProperties = ImmutableMap.of();
    }

    public TenacityPropertyStore(TenacityPropertyStoreBuilder<? extends Configuration> builder) {
        this.commandProperties = checkNotNull(builder.buildCommandProperties());
        this.threadpoolProperties = checkNotNull(builder.buildThreadpoolProperties());
    }

    public ImmutableMap<TenacityPropertyKey, TenacityCommandProperties.Setter> getCommandProperties() {
        return commandProperties;
    }

    public ImmutableMap<TenacityPropertyKey, TenacityThreadPoolProperties.Setter> getThreadpoolProperties() {
        return threadpoolProperties;
    }

    public void setCommandProperties(ImmutableMap<TenacityPropertyKey, TenacityCommandProperties.Setter> commandProperties) {
        this.commandProperties = commandProperties;
    }

    public void setThreadpoolProperties(ImmutableMap<TenacityPropertyKey, TenacityThreadPoolProperties.Setter> threadpoolProperties) {
        this.threadpoolProperties = threadpoolProperties;
    }

    public Optional<TenacityConfiguration> getTenacityConfiguration(TenacityPropertyKey samePropertiesKey) {
        return getTenacityConfiguration(samePropertiesKey, samePropertiesKey);
    }

    public Optional<TenacityConfiguration> getTenacityConfiguration(TenacityPropertyKey commandPropertiesKey,
                                                                    TenacityPropertyKey threadpoolPropertiesKey) {
        if (commandProperties.containsKey(commandPropertiesKey) && threadpoolProperties.containsKey(threadpoolPropertiesKey)) {
            final TenacityCommandProperties.Setter cmdProperties = commandProperties.get(commandPropertiesKey);
            final TenacityThreadPoolProperties.Setter tpProperties = threadpoolProperties.get(threadpoolPropertiesKey);

            final TenacityConfiguration configuration = new TenacityConfiguration(
                    new ThreadPoolConfiguration(
                            tpProperties.getCoreSize(),
                            tpProperties.getKeepAliveTimeMinutes(),
                            tpProperties.getMaxQueueSize(),
                            tpProperties.getQueueSizeRejectionThreshold(),
                            tpProperties.getMetricsRollingStatisticalWindowInMilliseconds(),
                            tpProperties.getMetricsRollingStatisticalWindowBuckets()),
                    new CircuitBreakerConfiguration(
                            cmdProperties.getCircuitBreakerRequestVolumeThreshold(),
                            cmdProperties.getCircuitBreakerSleepWindowInMilliseconds(),
                            cmdProperties.getCircuitBreakerErrorThresholdPercentage()),
                    cmdProperties.getExecutionIsolationThreadTimeoutInMilliseconds());
            return Optional.of(configuration);
        }
        return Optional.absent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenacityPropertyStore that = (TenacityPropertyStore) o;

        if (!commandProperties.equals(that.commandProperties)) return false;
        if (!threadpoolProperties.equals(that.threadpoolProperties)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = commandProperties.hashCode();
        result = 31 * result + threadpoolProperties.hashCode();
        return result;
    }
}