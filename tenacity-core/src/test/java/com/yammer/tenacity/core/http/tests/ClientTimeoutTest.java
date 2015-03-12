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
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.*;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            environment.jersey().register(barrierTarget);
        }
    }

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();
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
        return new JerseyClientBuilder(metricRegistry)
                .using(executorService, Jackson.newObjectMapper())
                .using(clientConfiguration)
                .build("test'");
    }

    private WebTarget postWithExpectedTimeout(Client client, Duration timeout) {
        boolean exceptionThrown = false;
        final WebTarget spyTarget = spy(client.target(uri));
        try {
            spyTarget
                    .queryParam("time", Long.toString(timeout.toMilliseconds()))
                    .request()
                    .post(Entity.text(null));
        } catch (Exception err) {
            exceptionThrown = err.getCause() instanceof SocketTimeoutException;
            synchronized (barrierTarget) {
                barrierTarget.notifyAll();
            }
        }

        assertTrue(exceptionThrown);
        return spyTarget;
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

        client.target(uri).queryParam("time", "100").request().post(null);
    }

    @Ignore("under investigation") // todo <michal>
    @Test
    public void jerseyClientTimeoutOverride() {
        clientConfiguration.setTimeout(Duration.milliseconds(100));
        final Client client = buildClient();

        setReadTimeout(client, Duration.seconds(2));
        client.target(uri).queryParam("time", "500").request().post(null);
    }


    @Ignore("under investigation") // todo <michal>
    @Test
    public void jerseyClientTimeoutOverrideToFail() {
        clientConfiguration.setTimeout(Duration.seconds(2));
        final Client client = buildClient();

        setReadTimeout(client, Duration.milliseconds(100));
        postWithExpectedTimeout(client, Duration.milliseconds(500));
    }

    @Ignore("under investigation") // todo <michal>
    @Test
    public void jerseyClientSupportsMultipleTimeoutChanges() {
        clientConfiguration.setTimeout(Duration.milliseconds(100));

        final Client client = buildClient();

        setReadTimeout(client, Duration.milliseconds(1));
        client.target(uri).queryParam("time", "500").request().post(null);

        setReadTimeout(client, Duration.milliseconds(100));
        postWithExpectedTimeout(client, Duration.milliseconds(500));

        setReadTimeout(client, Duration.milliseconds(500));
        client.target(uri).queryParam("time", "100"); // todo <michal> is this correct? it does nothing
    }

    @Ignore("under investigation") // todo <michal>
    @Test
    public void tenacityClientOverridesOriginalTimeout() {
        clientConfiguration.setTimeout(Duration.seconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(100);

        registerTenacityProperties();

        final WebTarget spyTarget = postWithExpectedTimeout(tenacityClientBuilder
                        .usingTimeoutPadding(Duration.milliseconds(43))
                        .build(buildClient()),
                Duration.milliseconds(500)
        );

        // todo <michal> this seems to whitebox, replace with behavioral?
        //       verify(spyTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 143);
    }

    @Ignore("under investigation") // todo <michal>
    @Test
    public void tenacityClientWorksWithChangingPropertiesDynamically() throws InterruptedException, ExecutionException {
        clientConfiguration.setTimeout(Duration.seconds(1));

        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(100);
        registerTenacityProperties();

        final Client client = tenacityClientBuilder.build(buildClient());

        WebTarget spyTarget = postWithExpectedTimeout(client, Duration.milliseconds(500));
        // todo <michal> behavioral?  verify(spyTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 150);

        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(500);
        registerTenacityProperties();

        spyTarget = spy(client.target("http://localhost:" + RULE.getLocalPort()));
        spyTarget.path("/").queryParam("time", "100").request().post(null);
        // todo <michal> behavioral?     verify(spyTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 550);
        final WebTarget spyAsyncTarget = spy(client.target("http://localhost:" + RULE.getLocalPort() + '/'));
        spyAsyncTarget.path("/").queryParam("time", "100").request().async().post(null).get();
        // todo <michal> behavioral?       verify(spyAsyncTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 550);

        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(325);
        registerTenacityProperties();

        spyTarget.path("/").queryParam("time", "100").request().post(null);
        // todo <michal> behavioral? verify(spyTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 375);
        spyAsyncTarget.path("/").queryParam("time", "100").request().async().post(null).get();
        // todo <michal> behavioral? verify(spyAsyncTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 375);
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
            webTarget.queryParam("time", Long.toString(sleepDuration.toMilliseconds())).request().post(null);
            return null;
        }
    }

    @Ignore("under investigation") // todo <michal>
    @Test
    public void tenacityDoesntRaceWithJerseyTimeout() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(300);
        registerTenacityProperties();

        final Client client = tenacityClientBuilder.build(buildClient());
        final WebTarget spyTarget = spy(client.target(uri));
        final VoidCommand command = new VoidCommand(spyTarget, Duration.milliseconds(500));

        boolean timeoutFailure = false;
        long startTime = System.currentTimeMillis();
        try {
            command.execute();
        } catch (HystrixRuntimeException err) {
            timeoutFailure = err.getFailureType().equals(HystrixRuntimeException.FailureType.TIMEOUT);
        }
        long endTime = System.currentTimeMillis();
        System.err.println("===============|" + (endTime - startTime));

        assertTrue(timeoutFailure);
        assertTrue(command.isResponseTimedOut());

        // todo <michal> verify(spyTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 350);
    }

    @Ignore("under investigation") // todo <michal>
    @Test
    public void adjustTimeoutOnWebTarget() {
        final WebTarget target = buildClient()
                .target(uri);

        target.getConfiguration()
                .getProperties()
                .put(ClientProperties.READ_TIMEOUT, 200);

        target.queryParam("time", "150")
                .request()
                .post(null);
    }

    @Ignore("under investigation") // todo <michal>
    @Test
    public void tenacityWebTargetAdjustTimeoutForPost() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(75); //will be 125ms after the 50ms buffer from the TenacityClientBuilder for 75+25=125ms
        registerTenacityProperties();
        final Client tenacityClient = tenacityClientBuilder.build(buildClient());

        final WebTarget spyTarget = spy(tenacityClient.target(uri));
        spyTarget.request().post(null);

        // todo <michal> behavioral? verify(spyTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 125);
    }

    @Ignore("under investigation") // todo <michal>
    @Test // todo <michal> is this really needed, seems hard to maintain
    public void adjustTimeoutWhenUsingDifferentMethods() {
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(150);
        registerTenacityProperties();
        final Client tenacityClient = tenacityClientBuilder.build(buildClient());
        final WebTarget spyTarget = spy(tenacityClient.target(uri));

        int times = 0;

        spyTarget.request().accept(MediaType.TEXT_PLAIN_TYPE).post(null);
        // todo <michal> behavioral? verify(spyTarget, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().acceptLanguage(Locale.ENGLISH).post(null);
        // todo <michal> behavioral? verify(spyTarget, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().header(HttpHeaders.AUTHORIZATION, "Something").post(null);
        // todo <michal> behavioral? verify(spyTarget, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().head();
        // todo <michal> behavioral? verify(spyTarget.request(), times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().get(String.class);
        // todo <michal> behavioral? verify(spyTarget.request(), times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().put(null);
        // todo <michal> behavioral? verify(spyTarget.request(), times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().post(null);
        // todo <michal> behavioral? verify(spyTarget.request(), times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().options(String.class);
        // todo <michal> behavioral? verify(spyTarget.request(), times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().delete();
        // todo <michal> behavioral? verify(spyTarget.request(), times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().put(Entity.text("test"));
        // todo <michal> behavioral? verify(spyTarget.request(), times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request().method("POST");
        // todo <michal> behavioral?  verify(spyTarget.request(), times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);

        spyTarget.request()
                .header(HttpHeaders.AUTHORIZATION, "Something")
                .acceptLanguage(Locale.ENGLISH)
                .post(Entity.text(null));
        // todo <michal> behavioral?  verify(spyTarget, times(++times)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 200);
    }


    @Ignore("investigating now")
    @Test
    public void noTenacityConfigurationSetShouldUseDefault() {
        // this establishes the socket timeout on the http client (connectionTimeout is 100 and unmodified and connectionRequestTimeout is 500)
        clientConfiguration.setTimeout(Duration.milliseconds(1));
        final Client tenacityClient = tenacityClientBuilder.build(buildClient()); // this casues the property to be udpated, but so far does not seem to propagate in any way to the http client
//        final WebTarget spyTarget = spy(tenacityClient.target(uri));
//        spyTarget.request().post(null);
            tenacityClient.target(uri).request().post(null);

        // this row can be deleted - ultimately we don't care how it is done, but we want the timeout to be updated so that this test passes (we might want to add a timing out test)
        // todo <michal> behavioral?  verify(spyTarget, times(1)).setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, 1050); //Tenacity default + 50ms
    }



    private static void setReadTimeout(Client client, Duration duration) {
        client.property(ClientProperties.READ_TIMEOUT, (int) duration.toMilliseconds());
    }

}