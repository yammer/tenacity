package com.yammer.tenacity.core.bundle.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.bundle.TenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.StringTenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TenacityConfiguredBundleTest {
    @ClassRule
    public static final DropwizardAppRule<Configuration> APP_ONE =
            new DropwizardAppRule<>(TenacityApp.class, Resources.getResource("clientTimeoutTest.yml").getPath());
    @ClassRule
    public static final DropwizardAppRule<Configuration> APP_TWO =
            new DropwizardAppRule<>(TenacityApp.class, Resources.getResource("authenticatorTest.yml").getPath());

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void twoDropwizardAppsCanRunInSameJvmWithMetricsPlugin() {
        validateAppIsRunning(APP_ONE);
        validateAppIsRunning(APP_TWO);
    }

    private static void validateAppIsRunning(DropwizardAppRule<Configuration> app) {
        final Client client = new JerseyClientBuilder(app.getEnvironment()).build("appOne");
        final Response response = client
                .target(String.format("http://localhost:%d", app.getLocalPort()))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        response.close();
    }


    public static class TenacityApp extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(TenacityBundleBuilder
                    .newBuilder()
                    .configurationFactory(new TenacityBundleConfigurationFactory<Configuration>() {
                        @Override
                        public Map<TenacityPropertyKey, TenacityConfiguration> getTenacityConfigurations(Configuration applicationConfiguration) {
                            return ImmutableMap.of();
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
            environment.jersey().register(new SimpleResource());
        }
    }

    @Path("/")
    public static class SimpleResource {
        @GET
        public Response nothing() {
            return Response.noContent().build();
        }
    }
}
