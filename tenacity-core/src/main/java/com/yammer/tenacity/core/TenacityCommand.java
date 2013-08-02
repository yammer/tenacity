package com.yammer.tenacity.core;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public abstract class TenacityCommand<ReturnType> extends HystrixCommand<ReturnType> {
    protected TenacityCommand(TenacityPropertyKey tenacityPropertyKey) {
        super(HystrixCommand.Setter.withGroupKey(commandGroupKeyFrom("TENACITY"))
                .andCommandKey(commandKeyFrom(tenacityPropertyKey.toString()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(tenacityPropertyKey.toString())));
    }

    public static HystrixCommandGroupKey commandGroupKeyFrom(String key) {
        return HystrixCommandGroupKey.Factory.asKey(key);
    }

    public static HystrixCommandKey commandKeyFrom(String key) {
        return HystrixCommandKey.Factory.asKey(key);
    }

    public HystrixCommandProperties getCommandProperties() {
        return HystrixPropertiesFactory.getCommandProperties(getCommandKey(), null);
    }

    public HystrixThreadPoolProperties getThreadpoolProperties() {
        return HystrixPropertiesFactory.getThreadPoolProperties(getThreadPoolKey(), null);
    }

    public HystrixCommandMetrics getCommandMetrics() {
        return HystrixCommandMetrics.getInstance(getCommandKey());
    }

    public HystrixThreadPoolMetrics getThreadpoolMetrics() {
        return HystrixThreadPoolMetrics.getInstance(getThreadPoolKey());
    }

    @Override
    protected abstract ReturnType run() throws Exception;

    @Override
    protected abstract ReturnType getFallback();
}
