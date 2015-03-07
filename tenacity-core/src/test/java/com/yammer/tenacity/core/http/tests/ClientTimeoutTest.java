package com.yammer.tenacity.core.http.tests;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.sun.jersey.api.client.AsyncWebResource;
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

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Locale;
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

        @HEAD
        public void head(@QueryParam("time")
                         @DefaultValue("100") long sleepTimeMs) throws InterruptedException {
            doSleep(sleepTimeMs);
        }

        @GET
        public String get(@QueryParam("time")
                         @DefaultValue("100") long sleepTimeMs) throws InterruptedException {
            doSleep(sleepTimeMs);
            return "test";
        }

        @OPTIONS
        public String options(@QueryParam("time")
                         @DefaultValue("100") long sleepTimeMs) throws InterruptedException {
            doSleep(sleepTimeMs);
            return "test";
        }

        @PUT
        public void put(@QueryParam("time")
                         @DefaultValue("100") long sleepTimeMs) throws InterruptedException {
            doSleep(sleepTimeMs);
        }

        @DELETE
        public void delete(@QueryParam("time")
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
            environment.jersey().register(barrierResource);
        }
    }

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();
    @ClassRule
    public static DropwizardAppRule<Configuration> RULE;
    private static final BarrierResource barrierResource = new BarrierResource();
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
        return new JerseyClientBuilder(metricRegistry)
                .using(executorService, Jackson.newObjectMapper())
                .using(clientConfiguration)
                .build("test'");
    }

    private WebResource postWithExpectedTimeout(Client client, Duration timeout) {
        boolean exceptionThrown = false;
        final WebResource spyResource = spy(client.resource(uri));
        try {
            spyResource.queryParam("time", Long.toString(timeout.toMilliseconds())).post();
        } catch (Exception err) {
            exceptionThrown = err.getCause() instanceof SocketTimeoutException;
            synchronized (barrierResource) {
                barrierResource.notifyAll();
            }
        }

        assertTrue(exceptionThrown);
        return spyResource;
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

        final WebResource spyResource = postWithExpectedTimeout(tenacityClientBuilder
                        .usingTimeoutPadding(Duration.milliseconds(43))
                        .build(buildClient()),
                Duration.milliseconds(500));

        verify(spyResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 143);
    }

    @Test
    public void tenacityClientWorksWithChangingPropertiesDynamically() throws InterruptedException, ExecutionException {
        clientConfiguration.setTimeout(Duration.seconds(1));

        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(100);
        registerTenacityProperties();

        final Client client = tenacityClientBuilder.build(buildClient());

        WebResource spyResource = postWithExpectedTimeout(client, Duration.milliseconds(500));
        verify(spyResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 150);

        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(500);
        registerTenacityProperties();

        spyResource = spy(client.resource("http://localhost:" + RULE.getLocalPort()));
        spyResource.path("/").queryParam("time", "100").post();
        verify(spyResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 550);
        final AsyncWebResource spyAsyncResource = spy(client.asyncResource("http://localhost:" + RULE.getLocalPort() + '/'));
        spyAsyncResource.path("/").queryParam("time", "100").post().get();
        verify(spyAsyncResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 550);

        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(325);
        registerTenacityProperties();

        spyResource.path("/").queryParam("time", "100").post();
        verify(spyResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 375);
        spyAsyncResource.path("/").queryParam("time", "100").post().get();
        verify(spyAsyncResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 375);
    }

    private static class VoidCommand extends TenacityCommand<Void> {
        private final WebResource webResource;
        private final Duration sleepDuration;

        public VoidCommand(WebResource webResource, Duration sleepDuration) {
            super(DependencyKey.CLIENT_TIMEOUT);
            this.webResource = webResource;
            this.sleepDuration = sleepDuration;
        }

        @Override
        protected Void run() throws Exception {
            webResource.queryParam("time", Long.toString(sleepDuration.toMilliseconds())).post();
            return null;
        }
    }

    @Test
    public void tenacityDoesntRaceWithJerseyTimeout() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(300);
        registerTenacityProperties();

        final Client client = tenacityClientBuilder.build(buildClient());
        final WebResource spyResource = spy(client.resource(uri));
        final VoidCommand command = new VoidCommand(spyResource, Duration.milliseconds(500));

        boolean timeoutFailure = false;
        try {
            command.execute();
        } catch (HystrixRuntimeException err) {
            timeoutFailure = err.getFailureType().equals(HystrixRuntimeException.FailureType.TIMEOUT);
        }

        assertTrue(timeoutFailure);
        assertTrue(command.isResponseTimedOut());

        verify(spyResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 350);
    }

    @Test
    public void adjustTimeoutOnWebResource() {
        final WebResource resource = buildClient()
                .resource(uri);
        resource.setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);
        resource.queryParam("time", "150").post();
    }

    @Test
    public void tenacityWebResourceAdjustTimeoutForPost() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(75); //will be 125ms after the 50ms buffer from the TenacityClientBuilder for 75+25=125ms
        registerTenacityProperties();
        final Client tenacityClient = tenacityClientBuilder.build(buildClient());

        final WebResource spyResource = spy(tenacityClient.resource(uri));
        spyResource.post();

        verify(spyResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 125);
    }

    @Test
    public void adjustTimeoutWhenUsingDifferentMethods() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(150);
        registerTenacityProperties();
        final Client tenacityClient = tenacityClientBuilder.build(buildClient());
        final WebResource spyResource = spy(tenacityClient.resource(uri));

        int times = 0;

        spyResource.accept(MediaType.TEXT_PLAIN_TYPE).post();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.acceptLanguage(Locale.ENGLISH).post();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.header(HttpHeaders.AUTHORIZATION, "Something").post();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.head();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.get(String.class);
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.put();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.post();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.options(String.class);
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.delete();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.entity("test").put();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.type(MediaType.TEXT_PLAIN_TYPE).post();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.method("POST");
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource
                .header(HttpHeaders.AUTHORIZATION, "Something")
                .type(MediaType.TEXT_PLAIN_TYPE)
                .acceptLanguage(Locale.ENGLISH)
                .post();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);
    }

    @Test
    public void adjustTimeoutWhenUsingDifferentMethodsForAsync() throws InterruptedException, ExecutionException  {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(150);
        registerTenacityProperties();
        final Client tenacityClient = tenacityClientBuilder.build(buildClient());
        final AsyncWebResource spyResource = spy(tenacityClient.asyncResource(uri));

        int times = 0;

        spyResource.accept(MediaType.TEXT_PLAIN_TYPE).post().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.acceptLanguage(Locale.ENGLISH).post().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.header(HttpHeaders.AUTHORIZATION, "Something").post().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.head().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.get(String.class).get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.put().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.post().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.options(String.class).get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.delete().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.entity("test").put().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.type(MediaType.TEXT_PLAIN_TYPE).post().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource.method("POST").get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyResource
                .header(HttpHeaders.AUTHORIZATION, "Something")
                .type(MediaType.TEXT_PLAIN_TYPE)
                .acceptLanguage(Locale.ENGLISH)
                .post().get();
        verify(spyResource, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);
    }

    @Test
    public void noTenacityConfigurationSetShouldUseDefault() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        final Client tenacityClient = tenacityClientBuilder.build(buildClient());
        final WebResource spyResource = spy(tenacityClient.resource(uri));
        spyResource.post();

        verify(spyResource, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 1050); //Tenacity default + 50ms
    }
}