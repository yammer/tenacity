package com.yammer.tenacity.core.http.tests;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
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
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.junit.*;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ClientTimeoutTest {
    @Path("/")
    public static class BarrierResource {
        @POST
        public void sleep(@QueryParam("time")
                          @DefaultValue("2000") long sleepTimeMs) throws InterruptedException {
            synchronized (this) {
                wait(sleepTimeMs);
            }
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
            environment.jersey().register(barrierResource);
        }
    }

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();
    @ClassRule
    public static DropwizardAppRule<Configuration> RULE;
    private static final BarrierResource barrierResource = new BarrierResource();
    private final URI uri = URI.create("http://localhost:8080");
    private JerseyClientConfiguration clientConfiguration;
    private TenacityConfiguration tenacityConfiguration;
    private MetricRegistry metricRegistry;
    private ExecutorService executorService;
    private final TenacityJerseyClientBuilder tenacityClientBuilder = TenacityJerseyClientBuilder.builder(DependencyKey.CLIENT_TIMEOUT);

    static {
        try {
            RULE = new DropwizardAppRule<>(ClientTimeoutApplication.class, fixture("clientTimeoutTest.yml"));
        } catch (IOException err) {
            Throwables.propagate(err);
        }
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
        return new JerseyClientBuilder(metricRegistry)
                .using(executorService, Jackson.newObjectMapper())
                .using(clientConfiguration)
                .build("test'");
    }

    private void postWithExpectedTimeout(Client client, Duration timeout) {
        boolean exceptionThrown = false;
        try {
            client.resource(uri).queryParam("time", Long.toString(timeout.toMilliseconds())).post();
        } catch (Exception err) {
            exceptionThrown = err.getCause() instanceof SocketTimeoutException;
            synchronized (barrierResource) {
                barrierResource.notifyAll();
            }
        }

        assertTrue(exceptionThrown);
    }

    private void registerTenacityProperties() {
        new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.CLIENT_TIMEOUT, tenacityConfiguration),
                new BreakerboxConfiguration(),
                mock(ArchaiusPropertyRegister.class))
        .register();
    }

    @Test
    public void originalJerseyClientTimesOut() {
        clientConfiguration.setTimeout(Duration.milliseconds(100));
        postWithExpectedTimeout(buildClient(), Duration.seconds(2));
    }

    @Test
    public void originalJerseyClientDoesntTimeOut() {
        clientConfiguration.setTimeout(Duration.seconds(2));
        final Client client = buildClient();

        client.resource(uri).queryParam("time", "100").post();
    }

    @Test
    public void jerseyClientTimeoutOverride() {
        clientConfiguration.setTimeout(Duration.milliseconds(100));
        final Client client = buildClient();

        client.setReadTimeout((int)Duration.seconds(2).toMilliseconds());
        client.resource(uri).queryParam("time", "500").post();
    }

    @Test
    public void jerseyClientTimeoutOverrideToFail() {
        clientConfiguration.setTimeout(Duration.seconds(2));
        final Client client = buildClient();

        client.setReadTimeout((int)Duration.milliseconds(100).toMilliseconds());
        postWithExpectedTimeout(client, Duration.milliseconds(500));
    }

    @Test
    public void jerseyClientSupportsMultipleTimeoutChanges() {
        clientConfiguration.setTimeout(Duration.milliseconds(100));

        final Client client = buildClient();

        client.setReadTimeout((int)Duration.seconds(1).toMilliseconds());
        client.resource(uri).queryParam("time", "500").post();

        client.setReadTimeout((int)Duration.milliseconds(100).toMilliseconds());
        postWithExpectedTimeout(client, Duration.milliseconds(500));

        client.setReadTimeout((int)Duration.milliseconds(500).toMilliseconds());
        client.resource(uri).queryParam("time", "100");
    }

    @Test
    public void tenacityClientOverridesOriginalTimeout() {
        clientConfiguration.setTimeout(Duration.seconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(100);

        registerTenacityProperties();

        final Client spyClient = spy(buildClient());

        postWithExpectedTimeout(tenacityClientBuilder
                        .usingTimeoutPadding(Duration.milliseconds(43))
                        .build(spyClient),
                Duration.milliseconds(500));

        verify(spyClient, times(1)).setReadTimeout(143);
    }

    @Test
    public void tenacityClientWorksWithChangingPropertiesDynamically() throws InterruptedException, ExecutionException {
        clientConfiguration.setTimeout(Duration.seconds(1));

        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(100);
        registerTenacityProperties();

        final Client spyClient = spy(buildClient());
        final Client client = tenacityClientBuilder.build(spyClient);

        postWithExpectedTimeout(client, Duration.milliseconds(500));
        verify(spyClient, times(1)).setReadTimeout(150);

        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(500);
        registerTenacityProperties();

        client.resource("http://localhost:8080").path("/").queryParam("time", "100").post();
        verify(spyClient, times(1)).setReadTimeout(550);
        client.asyncResource("http://localhost:8080/").path("/").queryParam("time", "100").post().get();
        verify(spyClient, times(2)).setReadTimeout(550);
    }

    private static class VoidCommand extends TenacityCommand<Void> {
        private final URI uri;
        private final Client client;
        private final Duration sleepDuration;

        public VoidCommand(URI uri, Client client, Duration sleepDuration) {
            super(DependencyKey.CLIENT_TIMEOUT);
            this.uri = uri;
            this.client = client;
            this.sleepDuration = sleepDuration;
        }

        @Override
        protected Void run() throws Exception {
            client.resource(uri).queryParam("time", Long.toString(sleepDuration.toMilliseconds())).post();
            return null;
        }
    }

    @Test
    public void tenacityDoesntRaceWithJerseyTimeout() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(100);
        registerTenacityProperties();

        final Client spyClient = spy(buildClient());
        final VoidCommand command = new VoidCommand(
                uri,
                tenacityClientBuilder.build(spyClient),
                Duration.milliseconds(500));

        boolean timeoutFailure = false;
        try {
            command.execute();
        } catch (HystrixRuntimeException err) {
            timeoutFailure = err.getFailureType().equals(HystrixRuntimeException.FailureType.TIMEOUT);
        }

        assertTrue(timeoutFailure);
        assertTrue(command.isResponseTimedOut());

        verify(spyClient, times(1)).setReadTimeout(150);
    }

    @Test
    public void adjustTimeoutOnWebResource() {
        final WebResource resource = buildClient()
                .resource(uri);
        resource.setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);
        resource.queryParam("time", "150").post();
    }
}