package com.yammer.tenacity.core.bundle;

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
import com.yammer.tenacity.core.servlets.TenacityCircuitBreakersServlet;
import com.yammer.tenacity.core.servlets.TenacityConfigurationServlet;
import com.yammer.tenacity.core.servlets.TenacityPropertyKeysServlet;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class TenacityConfiguredBundle<T extends Configuration> implements ConfiguredBundle<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityConfiguredBundle.class);
    protected final TenacityBundleConfigurationFactory<T> tenacityBundleConfigurationFactory;
    protected Optional<HystrixCommandExecutionHook> executionHook = Optional.empty();
    protected final Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers;
    protected final boolean usingTenacityCircuitBreakerHealthCheck;
    protected final boolean usingAdminPort;

    public TenacityConfiguredBundle(
            TenacityBundleConfigurationFactory<T> tenacityBundleConfigurationFactory,
            Optional<HystrixCommandExecutionHook> hystrixCommandExecutionHook,
            Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers,
            boolean usingTenacityCircuitBreakerHealthCheck,
            boolean usingAdminPort) {
        this.exceptionMappers = exceptionMappers;
        this.tenacityBundleConfigurationFactory = checkNotNull(tenacityBundleConfigurationFactory);
        this.executionHook = hystrixCommandExecutionHook;
        this.usingTenacityCircuitBreakerHealthCheck = usingTenacityCircuitBreakerHealthCheck;
        this.usingAdminPort = usingAdminPort;
    }

    public TenacityConfiguredBundle(
            TenacityBundleConfigurationFactory<T> tenacityBundleConfigurationFactory,
            Optional<HystrixCommandExecutionHook> hystrixCommandExecutionHook,
            Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers) {
        this(tenacityBundleConfigurationFactory, hystrixCommandExecutionHook, exceptionMappers, false, false);
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
        try {
            HystrixPlugins.getInstance().registerMetricsPublisher(new YammerMetricsPublisher(bootstrap.getMetricRegistry()));
        } catch (Exception err) {
            LOGGER.warn("Failed to register YammerMetricsPublisher with HystrixPlugins. This is what MetricsPublisher is currently registered: {}",
                    HystrixPlugins.getInstance().getMetricsPublisher().getClass(), err);
        }
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
        final ServerFactory serverFactory = configuration.getServerFactory();
        final Duration shutdownGracePeriod =
                (serverFactory instanceof AbstractServerFactory)
                ? ((AbstractServerFactory) serverFactory).getShutdownGracePeriod()
                : Duration.seconds(30L);
        environment.lifecycle().manage(new ManagedHystrix(shutdownGracePeriod));
    }

    protected void addTenacityResources(Environment environment,
                                        TenacityPropertyKeyFactory keyFactory,
                                        Collection<TenacityPropertyKey> tenacityPropertyKeys) {
        final String tenacityMetricsStream = "/tenacity/metrics.stream";
        final TenacityConfigurationResource configurationResource = new TenacityConfigurationResource(keyFactory);
        final TenacityCircuitBreakersResource circuitBreakersResource =
                new TenacityCircuitBreakersResource(tenacityPropertyKeys, keyFactory);
        final TenacityPropertyKeysResource propertyKeysResource = new TenacityPropertyKeysResource(tenacityPropertyKeys);

        if (usingAdminPort) {
            environment.admin()
                    .addServlet(tenacityMetricsStream, new HystrixMetricsStreamServlet())
                    .addMapping(tenacityMetricsStream);
            environment.admin()
                    .addServlet(TenacityPropertyKeysResource.PATH,
                            new TenacityPropertyKeysServlet(environment.getObjectMapper(), propertyKeysResource))
                    .addMapping(TenacityPropertyKeysResource.PATH);
            environment.admin()
                    .addServlet(TenacityConfigurationResource.PATH,
                            new TenacityConfigurationServlet(environment.getObjectMapper(), configurationResource))
                    .addMapping(TenacityConfigurationResource.PATH + "/*");
            environment.admin()
                    .addServlet(TenacityCircuitBreakersResource.PATH,
                            new TenacityCircuitBreakersServlet(environment.getObjectMapper(), circuitBreakersResource))
                    .addMapping(TenacityCircuitBreakersResource.PATH + "/*");
        } else {
            environment.servlets()
                    .addServlet(tenacityMetricsStream, new HystrixMetricsStreamServlet())
                    .addMapping(tenacityMetricsStream);
            environment.jersey().register(propertyKeysResource);
            environment.jersey().register(configurationResource);
            environment.jersey().register(circuitBreakersResource);
        }
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