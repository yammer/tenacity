package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.bundle.BaseTenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.bundle.TenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.bundle.TenacityConfiguredBundle;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.healthcheck.TenacityCircuitBreakerHealthCheck;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.core.properties.StringTenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


public class TenacityConfiguredBundleBuilderTest {
    public static class TenacityBundleApp extends Application<Configuration> {
        public static void main(String[] args) throws Exception {
            new TenacityBundleApp().run(args);
        }

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(TenacityBundleBuilder
                    .newBuilder()
                    .configurationFactory(CONFIGURATION_FACTORY)
                    .withCircuitBreakerHealthCheck()
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(
            TenacityBundleApp.class, Resources.getResource("clientTimeoutTest.yml").getPath());

    private static final TenacityBundleConfigurationFactory<Configuration> CONFIGURATION_FACTORY = new BaseTenacityBundleConfigurationFactory<Configuration>() {
        @Override
        public TenacityPropertyKeyFactory getTenacityPropertyKeyFactory(Configuration applicationConfiguration) {
            return new StringTenacityPropertyKeyFactory();
        }
    };

    @Test
    public void shouldBuild() {
        TenacityConfiguredBundle<Configuration> bundle =
                TenacityBundleBuilder.newBuilder()
                        .configurationFactory(CONFIGURATION_FACTORY)
                        .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        CONFIGURATION_FACTORY,
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
                .configurationFactory(CONFIGURATION_FACTORY)
                .addExceptionMapper(new TenacityExceptionMapper(429))
                .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        CONFIGURATION_FACTORY,
                        Optional.<HystrixCommandExecutionHook>absent(),
                        ImmutableList.<ExceptionMapper<? extends Throwable>>of(new TenacityExceptionMapper(429))
                ));
    }

    @Test
    public void useAllExceptionMappers() {
        final TenacityConfiguredBundle<Configuration> bundle = TenacityBundleBuilder
                .newBuilder()
                .configurationFactory(CONFIGURATION_FACTORY)
                .mapAllHystrixRuntimeExceptionsTo(429)
                .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        CONFIGURATION_FACTORY,
                        Optional.<HystrixCommandExecutionHook>absent(),
                        ImmutableList.<ExceptionMapper<? extends Throwable>>of(
                                new TenacityExceptionMapper(429),
                                new TenacityContainerExceptionMapper(429))
                ));
    }

    @Test
    public void withExecutionMappers() throws Exception {
        final HystrixCommandExecutionHook hook = new ExceptionLoggingCommandHook();
        final TenacityConfiguredBundle<Configuration> bundle = TenacityBundleBuilder
                .newBuilder()
                .configurationFactory(CONFIGURATION_FACTORY)
                .commandExecutionHook(hook)
                .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        CONFIGURATION_FACTORY,
                        Optional.of(hook),
                        Collections.<ExceptionMapper<? extends Throwable>>emptyList()
                ));
    }

    @Test
    public void withTenacityCircuitBreakerHealthCheck() {
        final TenacityConfiguredBundle<Configuration> bundle = TenacityBundleBuilder
                .newBuilder()
                .configurationFactory(CONFIGURATION_FACTORY)
                .withCircuitBreakerHealthCheck()
                .build();

        assertThat(bundle)
                .isEqualTo(new TenacityConfiguredBundle<>(
                        CONFIGURATION_FACTORY,
                        Optional.<HystrixCommandExecutionHook>absent(),
                        Collections.<ExceptionMapper<? extends Throwable>>emptyList(),
                        true,
                        false
                ));
    }
    
    @Test
    public void dropwizardClientRuleShouldAddTenacityCircuitBreakerHealthCheck() {
        assertThat(RULE.getEnvironment().healthChecks().getNames())
                .contains(new TenacityCircuitBreakerHealthCheck(Collections.<TenacityPropertyKey>emptyList()).getName());
    }
}