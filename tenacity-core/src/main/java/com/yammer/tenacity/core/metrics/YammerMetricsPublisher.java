package com.yammer.tenacity.core.metrics;

import com.codahale.metrics.MetricRegistry;
import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;

public class YammerMetricsPublisher extends HystrixMetricsPublisher {
    protected final MetricRegistry metricRegistry;

    public YammerMetricsPublisher(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public HystrixMetricsPublisherCommand getMetricsPublisherForCommand(HystrixCommandKey commandKey, HystrixCommandGroupKey commandGroupKey, HystrixCommandMetrics metrics, HystrixCircuitBreaker circuitBreaker, HystrixCommandProperties properties) {
        return new YammerMetricsPublisherCommand(commandKey, commandGroupKey, metrics, circuitBreaker, properties, metricRegistry);
    }

    @Override
    public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolMetrics metrics, HystrixThreadPoolProperties properties) {
        return super.getMetricsPublisherForThreadPool(threadPoolKey, metrics, properties);
    }
}