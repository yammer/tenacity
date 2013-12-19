package com.yammer.tenacity.core;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public abstract class TenacityCommand<ReturnType> extends HystrixCommand<ReturnType> {
    protected TenacityCommand(TenacityPropertyKey tenacityPropertyKey) {
        super(HystrixCommand.Setter.withGroupKey(commandGroupKeyFrom("TENACITY"))
                .andCommandKey(commandKeyFrom(tenacityPropertyKey))
                .andThreadPoolKey(threadpoolKeyFrom(tenacityPropertyKey)));
    }

    public static HystrixCommandGroupKey commandGroupKeyFrom(String key) {
        return HystrixCommandGroupKey.Factory.asKey(key);
    }

    public static HystrixCommandKey commandKeyFrom(TenacityPropertyKey key) {
        return HystrixCommandKey.Factory.asKey(key.toString());
    }

    public static HystrixThreadPoolKey threadpoolKeyFrom(TenacityPropertyKey key) {
        return HystrixThreadPoolKey.Factory.asKey(key.toString());
    }

    public static HystrixCommandProperties getCommandProperties(TenacityPropertyKey key) {
        return HystrixPropertiesFactory.getCommandProperties(commandKeyFrom(key), null);
    }

    public HystrixCommandProperties getCommandProperties() {
        return HystrixPropertiesFactory.getCommandProperties(getCommandKey(), null);
    }

    public static HystrixThreadPoolProperties getThreadpoolProperties(TenacityPropertyKey key) {
        return HystrixPropertiesFactory.getThreadPoolProperties(threadpoolKeyFrom(key), null);
    }

    public HystrixThreadPoolProperties getThreadpoolProperties() {
        return HystrixPropertiesFactory.getThreadPoolProperties(getThreadPoolKey(), null);
    }

    public HystrixCommandMetrics getCommandMetrics() {
        return HystrixCommandMetrics.getInstance(getCommandKey());
    }

    public static HystrixCommandMetrics getCommandMetrics(TenacityPropertyKey key) {
        return HystrixCommandMetrics.getInstance(commandKeyFrom(key));
    }

    public HystrixThreadPoolMetrics getThreadpoolMetrics() {
        return HystrixThreadPoolMetrics.getInstance(getThreadPoolKey());
    }

    public static HystrixThreadPoolMetrics getThreadpoolMetrics(TenacityPropertyKey key) {
        return HystrixThreadPoolMetrics.getInstance(threadpoolKeyFrom(key));
    }

    public HystrixCircuitBreaker getCircuitBreaker() {
        return HystrixCircuitBreaker.Factory.getInstance(getCommandKey());
    }

    public static HystrixCircuitBreaker getCircuitBreaker(TenacityPropertyKey key) {
        return HystrixCircuitBreaker.Factory.getInstance(commandKeyFrom(key));
    }

    @Override
    protected abstract ReturnType run() throws Exception;
}
