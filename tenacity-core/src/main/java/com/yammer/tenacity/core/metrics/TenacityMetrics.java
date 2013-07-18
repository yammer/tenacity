package com.yammer.tenacity.core.metrics;

import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.yammer.tenacity.core.TenacityCommand;

public class TenacityMetrics {
    private TenacityMetrics() {}
    
    public static HystrixCommandMetrics getCommandMetrics(TenacityCommand<?> tenacityCommand) {
        return HystrixCommandMetrics.getInstance(tenacityCommand.getCommandKey());
    }

    public static HystrixThreadPoolMetrics getThreadpoolMetrics(TenacityCommand<?> tenacityCommand) {
        return HystrixThreadPoolMetrics.getInstance(tenacityCommand.getThreadPoolKey());
    }
}