package com.yammer.tenacity.core.legacy;

import com.google.common.base.Optional;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.contrib.yammermetricspublisher.HystrixYammerMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.core.bundle.AbstractTenacityPropertyKeys;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;
import com.yammer.tenacity.core.strategies.ManagedConcurrencyStrategy;

import javax.ws.rs.ext.ExceptionMapper;

public class TenacityBundle extends AbstractTenacityPropertyKeys implements Bundle {
    public TenacityBundle(TenacityPropertyKeyFactory keyFactory,
                          Iterable<TenacityPropertyKey> keys) {
        super(keyFactory, keys);
    }

    public TenacityBundle(TenacityPropertyKeyFactory keyFactory,
                          Iterable<TenacityPropertyKey> keys,
                          Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers,
                          Optional<HystrixCommandExecutionHook> executionHook) {
        super(keyFactory, keys, exceptionMappers, executionHook);
    }

    @Override
    public void initialize(Environment environment) {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new ManagedConcurrencyStrategy(environment));
        if (executionHook.isPresent()) {
            HystrixPlugins.getInstance().registerCommandExecutionHook(executionHook.get());
        }
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixYammerMetricsPublisher());
        environment.addServlet(new HystrixMetricsStreamServlet(), "/tenacity/metrics.stream");
        for (ExceptionMapper<?> exceptionMapper : exceptionMappers) {
            environment.addProvider(exceptionMapper);
        }
        environment.addResource(new TenacityPropertyKeysResource(keys));
        environment.addResource(new TenacityConfigurationResource(keyFactory));
        environment.addResource(new TenacityCircuitBreakersResource(keys));
    }
}