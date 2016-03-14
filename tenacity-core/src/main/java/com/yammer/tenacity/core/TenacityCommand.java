package com.yammer.tenacity.core;

import com.netflix.hystrix.*;
import com.netflix.hystrix.metric.consumer.CumulativeCommandEventCounterStream;
import com.netflix.hystrix.metric.consumer.RollingCommandEventCounterStream;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public abstract class TenacityCommand<R> extends HystrixCommand<R> {
    protected TenacityCommand(TenacityPropertyKey tenacityPropertyKey) {
        super(HystrixCommand.Setter.withGroupKey(tenacityGroupKey())
                .andCommandKey(tenacityPropertyKey)
                .andThreadPoolKey(tenacityPropertyKey));
    }

    static HystrixCommandGroupKey tenacityGroupKey() {
        return commandGroupKeyFrom("TENACITY");
    }

    public static HystrixCommandGroupKey commandGroupKeyFrom(String key) {
        return HystrixCommandGroupKey.Factory.asKey(key);
    }

    public static HystrixCommandProperties getCommandProperties(TenacityPropertyKey key) {
        return HystrixPropertiesFactory.getCommandProperties(key, null);
    }

    public HystrixCommandProperties getCommandProperties() {
        return HystrixPropertiesFactory.getCommandProperties(getCommandKey(), null);
    }

    public static HystrixThreadPoolProperties getThreadpoolProperties(TenacityPropertyKey key) {
        return HystrixPropertiesFactory.getThreadPoolProperties(key, null);
    }

    public HystrixThreadPoolProperties getThreadpoolProperties() {
        return HystrixPropertiesFactory.getThreadPoolProperties(getThreadPoolKey(), null);
    }

    public HystrixCommandMetrics getCommandMetrics() {
        return HystrixCommandMetrics.getInstance(getCommandKey());
    }

    public static HystrixCommandMetrics getCommandMetrics(TenacityPropertyKey key) {
        return HystrixCommandMetrics.getInstance(key);
    }

    public HystrixThreadPoolMetrics getThreadpoolMetrics() {
        return HystrixThreadPoolMetrics.getInstance(getThreadPoolKey());
    }

    public static HystrixThreadPoolMetrics getThreadpoolMetrics(TenacityPropertyKey key) {
        return HystrixThreadPoolMetrics.getInstance(key);
    }

    public HystrixCircuitBreaker getCircuitBreaker() {
        return HystrixCircuitBreaker.Factory.getInstance(getCommandKey());
    }

    public static HystrixCircuitBreaker getCircuitBreaker(TenacityPropertyKey key) {
        return HystrixCircuitBreaker.Factory.getInstance(key);
    }

    public CumulativeCommandEventCounterStream getCumulativeCommandEventCounterStream() {
        return CumulativeCommandEventCounterStream.getInstance(getCommandKey(), getCommandProperties());
    }

    public RollingCommandEventCounterStream getRollingCommandEventCounterStream() {
        return RollingCommandEventCounterStream.getInstance(getCommandKey(), getCommandProperties());
    }

    @Override
    protected abstract R run() throws Exception;
}
