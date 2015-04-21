package com.yammer.tenacity.core.bundle;

import com.google.common.base.Optional;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.ManagedHystrix;
import com.yammer.tenacity.core.healthcheck.TenacityCircuitBreakerHealthCheck;
import com.yammer.tenacity.core.metrics.YammerMetricsPublisher;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class TenacityConfiguredBundle<T extends Configuration> implements ConfiguredBundle<T> {
    protected final TenacityBundleConfigurationFactory<T> tenacityBundleConfigurationFactory;
    protected Optional<HystrixCommandExecutionHook> executionHook = Optional.absent();
    protected final Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers;
    protected final boolean usingTenacityCircuitBreakerHealthCheck;

    public TenacityConfiguredBundle(
            TenacityBundleConfigurationFactory<T> tenacityBundleConfigurationFactory,
            Optional<HystrixCommandExecutionHook> hystrixCommandExecutionHook,
            Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers,
            boolean usingTenacityCircuitBreakerHealthCheck) {
        this.exceptionMappers = exceptionMappers;
        this.tenacityBundleConfigurationFactory = checkNotNull(tenacityBundleConfigurationFactory);
        this.executionHook = hystrixCommandExecutionHook;
        this.usingTenacityCircuitBreakerHealthCheck = usingTenacityCircuitBreakerHealthCheck;
    }

    public TenacityBundleConfigurationFactory<T> getTenacityBundleConfigurationFactory() {
        return tenacityBundleConfigurationFactory;
    }

    public Optional<HystrixCommandExecutionHook> getExecutionHook() {
        return executionHook;
    }

    public Iterable<ExceptionMapper<? extends Throwable>> getExceptionMappers() {
        return exceptionMappers;
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        Map<TenacityPropertyKey, TenacityConfiguration> tenacityPropertyKeyConfigurations =
                tenacityBundleConfigurationFactory.getTenacityConfigurations(configuration);

        configureHystrix(configuration, environment);
        addExceptionMappers(environment);
        addHealthChecks(tenacityPropertyKeyConfigurations.keySet(), environment);
        addTenacityResources(
                environment,
                tenacityBundleConfigurationFactory.getTenacityPropertyKeyFactory(configuration),
                tenacityPropertyKeyConfigurations.keySet()
        );

        registerTenacityProperties(tenacityPropertyKeyConfigurations, configuration);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        HystrixPlugins.getInstance().registerMetricsPublisher(new YammerMetricsPublisher(bootstrap.getMetricRegistry()));
        if (executionHook.isPresent()) {
            HystrixPlugins.getInstance().registerCommandExecutionHook(executionHook.get());
        }
    }

    protected void registerTenacityProperties(Map<TenacityPropertyKey, TenacityConfiguration> tenacityPropertyKeyConfigurations,
                                              T configuration) {
        new TenacityPropertyRegister(
                tenacityPropertyKeyConfigurations,
                tenacityBundleConfigurationFactory.getBreakerboxConfiguration(configuration)
        ).register();
    }

    protected void addExceptionMappers(Environment environment) {
        for (ExceptionMapper<?> exceptionMapper : exceptionMappers) {
            environment.jersey().register(exceptionMapper);
        }
    }

    protected void addHealthChecks(Iterable<TenacityPropertyKey> keys, Environment environment) {
        if (usingTenacityCircuitBreakerHealthCheck) {
            final TenacityCircuitBreakerHealthCheck tenacityCircuitBreakerHealthCheck = new TenacityCircuitBreakerHealthCheck(keys);
            environment.healthChecks().register(tenacityCircuitBreakerHealthCheck.getName(), tenacityCircuitBreakerHealthCheck);
        }
    }

    protected void configureHystrix(T configuration, Environment environment) {
        environment.lifecycle().manage(new ManagedHystrix(
                ((DefaultServerFactory) configuration.getServerFactory()).getShutdownGracePeriod()));
        environment.servlets()
                .addServlet("hystrix-metrics", new HystrixMetricsStreamServlet())
                .addMapping("/tenacity/metrics.stream");
    }

    protected void addTenacityResources(Environment environment,
                                      TenacityPropertyKeyFactory keyFactory,
                                      Iterable<TenacityPropertyKey> tenacityPropertyKeys) {
        environment.jersey().register(new TenacityPropertyKeysResource(tenacityPropertyKeys));
        environment.jersey().register(new TenacityConfigurationResource(keyFactory));
        environment.jersey().register(new TenacityCircuitBreakersResource(tenacityPropertyKeys, keyFactory));
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenacityBundleConfigurationFactory, executionHook, exceptionMappers, usingTenacityCircuitBreakerHealthCheck);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TenacityConfiguredBundle<?> other = (TenacityConfiguredBundle) obj;
        return Objects.equals(this.tenacityBundleConfigurationFactory, other.tenacityBundleConfigurationFactory)
                && Objects.equals(this.executionHook, other.executionHook)
                && Objects.equals(this.exceptionMappers, other.exceptionMappers)
                && Objects.equals(this.usingTenacityCircuitBreakerHealthCheck, other.usingTenacityCircuitBreakerHealthCheck);
    }
}