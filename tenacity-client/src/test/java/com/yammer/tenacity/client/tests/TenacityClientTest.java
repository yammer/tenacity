package com.yammer.tenacity.client.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.netflix.hystrix.HystrixCommandProperties;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.client.TenacityClientBuilder;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.bundle.TenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.config.*;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.StringTenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TenacityClientTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    private static final TenacityConfiguration CLIENT_KEY_CONFIGURATION = new TenacityConfiguration(
            new ThreadPoolConfiguration(),
            new CircuitBreakerConfiguration(),
            new SemaphoreConfiguration(),
            2000,
            HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);

    public static class TenacityClientApp extends Application<Configuration> {
        public static void main(String[] args) throws Exception {
            new TenacityClientApp().run(args);
        }

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(TenacityBundleBuilder
                    .newBuilder()
                    .configurationFactory(new TenacityBundleConfigurationFactory<Configuration>() {
                        @Override
                        public Map<TenacityPropertyKey, TenacityConfiguration> getTenacityConfigurations(Configuration applicationConfiguration) {
                            return ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(
                                    TenacityClientPropertyKey.CLIENT_KEY, CLIENT_KEY_CONFIGURATION);
                        }

                        @Override
                        public TenacityPropertyKeyFactory getTenacityPropertyKeyFactory(Configuration applicationConfiguration) {
                            return new StringTenacityPropertyKeyFactory();
                        }

                        @Override
                        public BreakerboxConfiguration getBreakerboxConfiguration(Configuration applicationConfiguration) {
                            return new BreakerboxConfiguration();
                        }
                    })
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(
            TenacityClientApp.class, Resources.getResource("tenacityClientApp.yml").getPath());

    private enum TenacityClientPropertyKey implements TenacityPropertyKey {
        CLIENT_KEY
    }

    private enum NonExistentKey implements TenacityPropertyKey {
        NOPE
    }

    private static URI ROOT;
    private static TenacityClient TENACITYCLIENT;

    @BeforeClass
    public static void oneTimeSetup() {
        ROOT = URI.create("http://localhost:" + RULE.getLocalPort());
        TENACITYCLIENT = new TenacityClientBuilder(RULE.getEnvironment(), TenacityClientPropertyKey.CLIENT_KEY).build();
    }

    @Test
    public void getPropertyKeys() {
        assertThat(TENACITYCLIENT.getTenacityPropertyKeys(ROOT))
                .contains(ImmutableList.of(TenacityClientPropertyKey.CLIENT_KEY.name()));
    }

    @Test
    public void getTenacityConfiguration() {
        assertThat(TENACITYCLIENT.getTenacityConfiguration(ROOT, TenacityClientPropertyKey.CLIENT_KEY))
                .contains(CLIENT_KEY_CONFIGURATION);
        assertThat(TENACITYCLIENT.getTenacityConfiguration(ROOT, NonExistentKey.NOPE))
                .contains(new TenacityConfiguration(
                        new ThreadPoolConfiguration(),
                        new CircuitBreakerConfiguration(),
                        new SemaphoreConfiguration(),
                        1000,
                        HystrixCommandProperties.ExecutionIsolationStrategy.THREAD));
    }

    @Test
    public void getCircuitBreakers() {
        assertThat(TENACITYCLIENT.getCircuitBreakers(ROOT)).contains(Collections.<CircuitBreaker>emptyList());
        assertThat(TENACITYCLIENT.getCircuitBreaker(ROOT, TenacityClientPropertyKey.CLIENT_KEY))
                .isAbsent();

        assertTrue(new TenacityCommand<Boolean>(TenacityClientPropertyKey.CLIENT_KEY) {
            @Override
            protected Boolean run() throws Exception {
                return true;
            }
        }.execute());

        final Optional<ImmutableList<CircuitBreaker>> circuits = TENACITYCLIENT.getCircuitBreakers(ROOT);
        assertThat(circuits).isPresent();
        assertThat(circuits.get())
                .contains(CircuitBreaker.closed(TenacityClientPropertyKey.CLIENT_KEY));

        final Optional<CircuitBreaker> circuit = TENACITYCLIENT.getCircuitBreaker(ROOT, TenacityClientPropertyKey.CLIENT_KEY);
        assertThat(circuit).contains(CircuitBreaker.closed(TenacityClientPropertyKey.CLIENT_KEY));
    }

    @Test
    public void modifyCircuitBreakerForcedOpen() {
        assertThat(TENACITYCLIENT.modifyCircuitBreaker(ROOT, TenacityClientPropertyKey.CLIENT_KEY, CircuitBreaker.State.FORCED_OPEN))
                .isAbsent();

        assertThat(TENACITYCLIENT.getCircuitBreakers(ROOT))
                .contains(Collections.emptyList());

        assertFalse(new TenacityCommand<Boolean>(TenacityClientPropertyKey.CLIENT_KEY) {
            @Override
            protected Boolean run() throws Exception {
                return true;
            }

            @Override
            protected Boolean getFallback() {
                return false;
            }
        }.execute());

        assertThat(TENACITYCLIENT.getCircuitBreakers(ROOT))
                .contains(ImmutableList.of(CircuitBreaker.forcedOpen(TenacityClientPropertyKey.CLIENT_KEY)));
        assertThat(TENACITYCLIENT.getCircuitBreaker(ROOT, TenacityClientPropertyKey.CLIENT_KEY))
                .contains(CircuitBreaker.forcedOpen(TenacityClientPropertyKey.CLIENT_KEY));

        assertThat(TENACITYCLIENT.modifyCircuitBreaker(ROOT, TenacityClientPropertyKey.CLIENT_KEY, CircuitBreaker.State.FORCED_CLOSED))
                .contains(CircuitBreaker.forcedClosed(TenacityClientPropertyKey.CLIENT_KEY));

        assertTrue(new TenacityCommand<Boolean>(TenacityClientPropertyKey.CLIENT_KEY) {
            @Override
            protected Boolean run() throws Exception {
                return true;
            }

            @Override
            protected Boolean getFallback() {
                return false;
            }
        }.execute());

        assertThat(TENACITYCLIENT.getCircuitBreakers(ROOT))
                .contains(ImmutableList.of(CircuitBreaker.forcedClosed(TenacityClientPropertyKey.CLIENT_KEY)));
        assertThat(TENACITYCLIENT.getCircuitBreaker(ROOT, TenacityClientPropertyKey.CLIENT_KEY))
                .contains(CircuitBreaker.forcedClosed(TenacityClientPropertyKey.CLIENT_KEY));

        assertThat(TENACITYCLIENT.modifyCircuitBreaker(ROOT, TenacityClientPropertyKey.CLIENT_KEY, CircuitBreaker.State.FORCED_RESET))
                .contains(CircuitBreaker.closed(TenacityClientPropertyKey.CLIENT_KEY));
        assertThat(TENACITYCLIENT.getCircuitBreakers(ROOT))
                .contains(ImmutableList.of(CircuitBreaker.closed(TenacityClientPropertyKey.CLIENT_KEY)));
    }
}