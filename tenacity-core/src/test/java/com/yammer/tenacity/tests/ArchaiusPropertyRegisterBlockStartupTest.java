package com.yammer.tenacity.tests;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import com.google.common.primitives.Ints;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.bundle.TenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ArchaiusPropertyRegisterBlockStartupTest {
    @ClassRule
    public static final WireMockRule wireMockRule = new WireMockRule(55789);

    @Rule
    public TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Rule
    public final DropwizardAppRule<AppConfiguration> rule = new DropwizardAppRule<>(
            App.class, Resources.getResource("waitForInitialLoadApp.yml").getPath());

    private static class AppConfiguration extends Configuration {
        @NotNull @Valid
        private BreakerboxConfiguration breakerbox;

        public BreakerboxConfiguration getBreakerbox() {
            return breakerbox;
        }
    }

    public static class App extends Application<AppConfiguration> {
        public static void main(String[] args) throws Exception {
            new TenacityConfiguredBundleBuilderTest.TenacityBundleApp().run(args);
        }

        final CountDownLatch startedLatch = new CountDownLatch(1);

        @Override
        public void initialize(Bootstrap<AppConfiguration> bootstrap) {
            bootstrap.addBundle(TenacityBundleBuilder
                    .<AppConfiguration>newBuilder()
                    .configurationFactory(new TenacityBundleConfigurationFactory<AppConfiguration>() {
                        @Override
                        public Map<TenacityPropertyKey, TenacityConfiguration> getTenacityConfigurations(AppConfiguration applicationConfiguration) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public TenacityPropertyKeyFactory getTenacityPropertyKeyFactory(AppConfiguration applicationConfiguration) {
                            return new StringTenacityPropertyKeyFactory();
                        }

                        @Override
                        public BreakerboxConfiguration getBreakerboxConfiguration(AppConfiguration applicationConfiguration) {
                            return applicationConfiguration.getBreakerbox();
                        }
                    })
                    .build());
        }

        @Override
        public void run(AppConfiguration configuration, Environment environment) throws Exception {
            startedLatch.countDown();
        }
    }

    @BeforeClass
    public static void init() {
        wireMockRule.stubFor(get(urlEqualTo("/archaius/test"))
                .willReturn(aResponse()
                        .withFixedDelay(Ints.checkedCast(Duration.ofSeconds(2).toMillis()))
                        .withStatus(200)));
    }


    @Test
    public void waitingForInitialLoadBlocksDropwizardFromStarting() throws Exception {
        assertThat(((App)rule.getApplication()).startedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        verify(getRequestedFor(urlMatching("/archaius/test")));
    }

}
