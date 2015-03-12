package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.bundle.BaseTenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.bundle.TenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.bundle.TenacityConfiguredBundle;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import io.dropwizard.Configuration;
import org.junit.Test;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


public class TenacityConfiguredBundleBuilderTest {
    private final TenacityPropertyKeyFactory propertyKeyFactory = new TenacityPropertyKeyFactory() {
        @Override
        public TenacityPropertyKey from(String value) {
            return null;
        }
    };


    private final TenacityBundleConfigurationFactory<Configuration> configurationFactory = new BaseTenacityBundleConfigurationFactory<Configuration>() {
        @Override
        public TenacityPropertyKeyFactory getTenacityPropertyKeyFactory(Configuration applicationConfiguration) {
            return propertyKeyFactory;
        }
    };

    @Test
    public void shouldBuild() {
        TenacityConfiguredBundle<Configuration> bundle =
                TenacityBundleBuilder.newBuilder()
                        .configurationFactory(configurationFactory)
                        .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        configurationFactory,
                        Optional.<HystrixCommandExecutionHook>absent(),
                        Collections.<ExceptionMapper<? extends Throwable>>emptyList()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithoutConfigurationFactory() {
        TenacityBundleBuilder
                .newBuilder()
                .build();
    }

    @Test
    public void shouldUseExceptionMapper() {
        final TenacityConfiguredBundle<Configuration> bundle = TenacityBundleBuilder
                .newBuilder()
                .configurationFactory(configurationFactory)
                .addExceptionMapper(new TenacityExceptionMapper(429))
                .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        configurationFactory,
                        Optional.<HystrixCommandExecutionHook>absent(),
                        ImmutableList.<ExceptionMapper<? extends Throwable>>of(new TenacityExceptionMapper(429))));
    }

    @Test
    public void useAllExceptionMappers() {
        final TenacityConfiguredBundle<Configuration> bundle = TenacityBundleBuilder
                .newBuilder()
                .configurationFactory(configurationFactory)
                .mapAllHystrixRuntimeExceptionsTo(429)
                .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        configurationFactory,
                        Optional.<HystrixCommandExecutionHook>absent(),
                        ImmutableList.<ExceptionMapper<? extends Throwable>>of(
                                new TenacityExceptionMapper(429),
                                new TenacityContainerExceptionMapper(429))));
    }

    @Test
    public void withExecutionMappers() throws Exception {
        final HystrixCommandExecutionHook hook = new ExceptionLoggingCommandHook();
        final TenacityConfiguredBundle<Configuration> bundle = TenacityBundleBuilder
                .newBuilder()
                .configurationFactory(configurationFactory)
                .commandExecutionHook(hook)
                .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        configurationFactory,
                        Optional.of(hook),
                        Collections.<ExceptionMapper<? extends Throwable>>emptyList()));
    }
}