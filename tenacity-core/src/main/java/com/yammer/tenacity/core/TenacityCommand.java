package com.yammer.tenacity.core;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public abstract class TenacityCommand<ReturnType> extends HystrixCommand<ReturnType> {
    protected TenacityCommand(TenacityPropertyKey tenacityPropertyKey) {
        super(HystrixCommand.Setter.withGroupKey(commandGroupKeyFrom("TENACITY"))
                .andCommandKey(tenacityPropertyKey)
                .andThreadPoolKey(tenacityPropertyKey));
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

    @Override
    protected abstract ReturnType run() throws Exception;
}
