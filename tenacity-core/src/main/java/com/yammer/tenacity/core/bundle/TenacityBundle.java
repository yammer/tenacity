package com.yammer.tenacity.core.bundle;

import com.google.common.base.Optional;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.metrics.YammerMetricsPublisher;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;
import com.yammer.tenacity.core.strategies.ManagedConcurrencyStrategy;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
    public void initialize(Bootstrap<?> bootstrap) {
        HystrixPlugins.getInstance().registerMetricsPublisher(new YammerMetricsPublisher(bootstrap.getMetricRegistry()));
        if (executionHook.isPresent()) {
            HystrixPlugins.getInstance().registerCommandExecutionHook(executionHook.get());
        }
    }

    @Override
    public void run(Environment environment) {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new ManagedConcurrencyStrategy(environment));
        environment.servlets()
                .addServlet("hystrix-metrics", new HystrixMetricsStreamServlet())
                .addMapping("/tenacity/metrics.stream");
        for (ExceptionMapper<?> exceptionMapper : exceptionMappers) {
            environment.jersey().register(exceptionMapper);
        }
        environment.jersey().register(new TenacityPropertyKeysResource(keys));
        environment.jersey().register(new TenacityConfigurationResource(keyFactory));
        environment.jersey().register(new TenacityCircuitBreakersResource(keys));
    }
}