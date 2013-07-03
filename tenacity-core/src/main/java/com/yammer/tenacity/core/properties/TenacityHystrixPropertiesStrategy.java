package com.yammer.tenacity.core.properties;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;

public class TenacityHystrixPropertiesStrategy extends HystrixPropertiesStrategy {
    @Override
    public HystrixCommandProperties getCommandProperties(HystrixCommandKey commandKey,
                                                         HystrixCommandProperties.Setter builder) {
        return new TenacityCommandProperties(commandKey, builder);
    }

    @Override
    public TenacityThreadPoolProperties getThreadPoolProperties(HystrixThreadPoolKey threadPoolKey,
                                                                HystrixThreadPoolProperties.Setter builder) {
        return new TenacityThreadPoolProperties(threadPoolKey, builder);
    }
}
