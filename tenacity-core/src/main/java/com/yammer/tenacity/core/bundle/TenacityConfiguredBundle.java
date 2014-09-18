package com.yammer.tenacity.core.bundle;

import com.google.common.base.Optional;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.metrics.YammerMetricsPublisher;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;
import com.yammer.tenacity.core.strategies.ManagedConcurrencyStrategy;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class TenacityConfiguredBundle<T extends Configuration> implements ConfiguredBundle<T> {
    private final TenacityBundleConfigurationFactory<T> tenacityBundleConfigurationFactory;
    private Optional<HystrixCommandExecutionHook> executionHook = Optional.absent();
    private final Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers;

    public TenacityConfiguredBundle(
            TenacityBundleConfigurationFactory<T> tenacityBundleConfigurationFactory,
            Optional<HystrixCommandExecutionHook> hystrixCommandExecutionHook,
            Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers) {
        this.exceptionMappers = exceptionMappers;
        this.tenacityBundleConfigurationFactory = checkNotNull(tenacityBundleConfigurationFactory);
        this.executionHook = hystrixCommandExecutionHook;

    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        Map<TenacityPropertyKey, TenacityConfiguration> tenacityPropertyKeyConfigurations =
                tenacityBundleConfigurationFactory.getTenacityConfigurations(configuration);


        configureHystrix(environment);
        addExceptionMappers(environment);
        addTenacityResources(
                environment,
                tenacityBundleConfigurationFactory.getTenacityPropertyKeyFactory(configuration),
                tenacityPropertyKeyConfigurations.keySet()
        );

        new TenacityPropertyRegister(
                tenacityPropertyKeyConfigurations,
                tenacityBundleConfigurationFactory.getBreakerboxConfiguration(configuration)
        ).register();
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        HystrixPlugins.getInstance().registerMetricsPublisher(new YammerMetricsPublisher(bootstrap.getMetricRegistry()));
        if (executionHook.isPresent()) {
            HystrixPlugins.getInstance().registerCommandExecutionHook(executionHook.get());
        }
    }

    private void addExceptionMappers(Environment environment) {
        for (ExceptionMapper<?> exceptionMapper : exceptionMappers) {
            environment.jersey().register(exceptionMapper);
        }
    }

    private void configureHystrix(Environment environment) {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new ManagedConcurrencyStrategy(environment));
        environment.servlets()
                .addServlet("hystrix-metrics", new HystrixMetricsStreamServlet())
                .addMapping("/tenacity/metrics.stream");
    }

    private void addTenacityResources(Environment environment,
                                      TenacityPropertyKeyFactory keyFactory,
                                      Iterable<TenacityPropertyKey> tenacityPropertyKeys) {


        environment.jersey().register(new TenacityPropertyKeysResource(tenacityPropertyKeys));
        environment.jersey().register(new TenacityConfigurationResource(keyFactory));
        environment.jersey().register(new TenacityCircuitBreakersResource(tenacityPropertyKeys));
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenacityBundleConfigurationFactory, executionHook, exceptionMappers);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TenacityConfiguredBundle other = (TenacityConfiguredBundle) obj;
        return Objects.equals(this.tenacityBundleConfigurationFactory, other.tenacityBundleConfigurationFactory) &&
                Objects.equals(this.executionHook, other.executionHook) &&
                Objects.equals(this.exceptionMappers, other.exceptionMappers);
    }
}