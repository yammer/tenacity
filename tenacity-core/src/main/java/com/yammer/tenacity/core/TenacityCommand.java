package com.yammer.tenacity.core;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public abstract class TenacityCommand<ReturnType> extends HystrixCommand<ReturnType> {
    protected TenacityCommand(HystrixCommandGroupKey hystrixCommandGroupKey,
                              HystrixCommandKey hystrixCommandKey,
                              TenacityPropertyStore tenacityPropertyStore,
                              TenacityPropertyKey tenacityPropertyKey) {
        super(HystrixCommand.Setter.withGroupKey(hystrixCommandGroupKey)
                .andCommandKey(hystrixCommandKey)
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(tenacityPropertyKey.toString()))
                .andThreadPoolPropertiesDefaults(tenacityPropertyStore.getThreadpoolProperties().get(tenacityPropertyKey))
                .andCommandPropertiesDefaults(tenacityPropertyStore.getCommandProperties().get(tenacityPropertyKey)));
    }

    protected TenacityCommand(String commandGroupKey,
                              String commandKey,
                              TenacityPropertyStore tenacityPropertyStore,
                              TenacityPropertyKey tenacityPropertyKey) {
        this(commandGroupKeyFrom(commandGroupKey),
             commandKeyFrom(commandKey),
             tenacityPropertyStore,
             tenacityPropertyKey);
    }

    public static HystrixCommandGroupKey commandGroupKeyFrom(String key) {
        return HystrixCommandGroupKey.Factory.asKey(key);
    }

    public static HystrixCommandKey commandKeyFrom(String key) {
        return HystrixCommandKey.Factory.asKey(key);
    }

    @Override
    protected abstract ReturnType run() throws Exception;

    @Override
    protected abstract ReturnType getFallback();
}
