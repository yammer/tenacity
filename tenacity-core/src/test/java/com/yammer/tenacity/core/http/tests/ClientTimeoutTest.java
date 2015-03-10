package com.yammer.tenacity.core.http.tests;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.http.TenacityJerseyClientBuilder;
import com.yammer.tenacity.core.properties.ArchaiusPropertyRegister;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.testing.TenacityTestRule;
import com.yammer.tenacity.tests.DependencyKey;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ClientTimeoutTest {
    @Path("/")
    public static class BarrierTarget {
        private void doSleep(long time) throws InterruptedException {
            synchronized (this) {
                wait(time);
            }
        }

        @POST
        public void post(@QueryParam("time")
                         @DefaultValue("100") long sleepTimeMs) throws InterruptedException {
            doSleep(sleepTimeMs);
        }
    }

    public static class ClientTimeoutApplication extends Application<Configuration> {
        public static void main(String[] args) throws Exception {
            new ClientTimeoutApplication().run(args);
        }

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(barrierTarget);
        }
    }

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @ClassRule
    public static DropwizardAppRule<Configuration> RULE;
    private static final BarrierTarget barrierTarget = new BarrierTarget();
    private final URI uri = URI.create("http://localhost:" + RULE.getLocalPort());
    private JerseyClientConfiguration clientConfiguration;
    private TenacityConfiguration tenacityConfiguration;
    private MetricRegistry metricRegistry;
    private ExecutorService executorService;
    private final TenacityJerseyClientBuilder tenacityClientBuilder = TenacityJerseyClientBuilder.builder(DependencyKey.CLIENT_TIMEOUT);

    static {
        RULE = new DropwizardAppRule<>(ClientTimeoutApplication.class, Resources.getResource("clientTimeoutTest.yml").getPath());
    }

    @Before
    public void setup() {
        clientConfiguration = new JerseyClientConfiguration();
        clientConfiguration.setConnectionTimeout(Duration.milliseconds(100));
        tenacityConfiguration = new TenacityConfiguration();
        metricRegistry = new MetricRegistry();
        executorService = Executors.newSingleThreadExecutor();
    }

    @After
    public void teardown() {
        executorService.shutdown();
    }

    private Client buildClient() {
        return new PatchedJerseyClientBuilder(metricRegistry)
                .using(executorService, Jackson.newObjectMapper())
                .using(clientConfiguration)
                .build("test'");
    }

    private void registerTenacityProperties() {
        new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.CLIENT_TIMEOUT, tenacityConfiguration),
                new BreakerboxConfiguration(),
                mock(ArchaiusPropertyRegister.class))
                .register();
    }

    @Test
    public void tenacityClient_adds_padding_to_the_timeout() {
        clientConfiguration.setTimeout(Duration.seconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(480);
        registerTenacityProperties();
        final Client client = tenacityClientBuilder.build(buildClient());
        final WebTarget webTarget = client.target(uri);

        postSettingTheTimeoutOnResource(webTarget, Duration.milliseconds(500));
    }

    @Test
    public void tenacityClientWorksWithChangingPropertiesDynamically() throws InterruptedException, ExecutionException {
        clientConfiguration.setTimeout(Duration.seconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(100);
        registerTenacityProperties();
        final Client client = tenacityClientBuilder.build(buildClient());
        final WebTarget webTarget = client.target(uri);


        // with timeout too short
        thrown.expectCause(any(SocketTimeoutException.class));
        postSettingTheTimeoutOnResource(webTarget, Duration.milliseconds(500));

        // after reconfiguring a longer timeout
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(520);
        registerTenacityProperties();

        postSettingTheTimeoutOnResource(webTarget, Duration.milliseconds(500));
    }



    @Test
    public void tenacityDoesntRaceWithJerseyTimeout() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(300);
        registerTenacityProperties();

        final Client client = tenacityClientBuilder.build(buildClient());
        final WebTarget spyTarget = spy(client.target(uri));
        final VoidCommand command = new VoidCommand(spyTarget, Duration.milliseconds(500));

        boolean timeoutFailure = false;
        try {
            command.execute();
        } catch (HystrixRuntimeException err) {
            timeoutFailure = err.getFailureType().equals(HystrixRuntimeException.FailureType.TIMEOUT);
        }

        assertTrue(timeoutFailure);
        assertTrue(command.isResponseTimedOut());
    }

    @Test
    public void tenacity_configuration_overrides_default_configuration() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        final Client tenacityClient = tenacityClientBuilder.build(buildClient());

        postSettingTheTimeoutOnResource(tenacityClient.target(uri), Duration.milliseconds(100));

    }

    @Test
    public void regularClientTimesOut() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        final Client regularClientWithNoTenacityOverride = buildClient();

        thrown.expectCause(any(SocketTimeoutException.class));
        postSettingTheTimeoutOnResource(regularClientWithNoTenacityOverride.target(uri), Duration.milliseconds(100));
    }

    private static class VoidCommand extends TenacityCommand<Void> {
        private final WebTarget webTarget;
        private final Duration sleepDuration;

        public VoidCommand(WebTarget webTarget, Duration sleepDuration) {
            super(DependencyKey.CLIENT_TIMEOUT);
            this.webTarget = webTarget;
            this.sleepDuration = sleepDuration;
        }

        @Override
        protected Void run() throws Exception {
            postSettingTheTimeoutOnResource(webTarget, sleepDuration);
            return null;
        }
    }

    private static void postSettingTheTimeoutOnResource(WebTarget webTarget, Duration timeout) {
        webTarget
                .queryParam("time", Long.toString(timeout.toMilliseconds()))
                .request()
                .post(Entity.text(null));
    }

}