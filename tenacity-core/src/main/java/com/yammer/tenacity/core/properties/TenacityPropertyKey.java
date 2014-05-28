package com.yammer.tenacity.core.properties;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;

public interface TenacityPropertyKey extends HystrixCommandKey, HystrixThreadPoolKey {
}