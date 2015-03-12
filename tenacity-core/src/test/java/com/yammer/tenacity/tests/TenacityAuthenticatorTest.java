package com.yammer.tenacity.tests;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.tenacity.core.auth.TenacityAuthenticator;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.logging.DefaultExceptionLogger;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.core.properties.ArchaiusPropertyRegister;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TenacityAuthenticatorTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    private ExecutorService executorService;
    private Authenticator<String, Object> mockAuthenticator;
    private Authenticator<String, Object> tenacityAuthenticator;
    private TenacityExceptionMapper tenacityExceptionMapper = spy(new TenacityExceptionMapper());
    private TenacityContainerExceptionMapper tenacityContainerExceptionMapper =
            spy(new TenacityContainerExceptionMapper());

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE =
            new DropwizardAppRule<>(AuthenticatorApp.class, Resources.getResource("clientTimeoutTest.yml").getPath());

    @Rule
    public final ResourceTestRule resources;

    {
        mockAuthenticator = mock(Authenticator.class);
        tenacityAuthenticator = TenacityAuthenticator.wrap(mockAuthenticator, DependencyKey.TENACITY_AUTH_TIMEOUT);
        resources = ResourceTestRule.builder()
                .addResource(new AuthResource())
                .addProvider(AuthFactory.binder(new OAuthFactory<>(tenacityAuthenticator, "test-realm", Object.class)))
                .addProvider(tenacityExceptionMapper)
                .addProvider(tenacityContainerExceptionMapper)
                .build();
    }

    @Before
    public void setup() {
        reset(mockAuthenticator);
        reset(AuthenticatorApp.getMockAuthenticator());
        reset(tenacityExceptionMapper);
        reset(AuthenticatorApp.getTenacityExceptionMapper());
        reset(tenacityContainerExceptionMapper);
        reset(AuthenticatorApp.getTenacityContainerExceptionMapper());
        executorService = Executors.newSingleThreadExecutor();
    }

    @After
    public void teardown() {
        executorService.shutdown();
    }

    @Path("/auth")
    public static class AuthResource {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Response alwaysThrow(@Auth Object principal) {
            return Response.ok().build();
        }
    }

    @Test(expected = HystrixRuntimeException.class)
    public void shouldThrowWhenAuthenticateTimesOut() throws AuthenticationException {
        final TenacityConfiguration overrideConfiguration = new TenacityConfiguration();
        overrideConfiguration.setExecutionIsolationThreadTimeoutInMillis(1);

        new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.TENACITY_AUTH_TIMEOUT, overrideConfiguration),
                new BreakerboxConfiguration(),
                mock(ArchaiusPropertyRegister.class))
                .register();

        when(mockAuthenticator.authenticate(any(String.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(50);
                return new Object();
            }
        });

        try {
            assertThat(tenacityAuthenticator.authenticate("credentials"))
                    .isAbsent();
        } catch (HystrixRuntimeException err) {
            assertThat(err.getFailureType()).isEqualTo(HystrixRuntimeException.FailureType.TIMEOUT);
            throw err;
        }
    }

    @Test
    public void shouldLogWhenExceptionIsThrown() throws AuthenticationException {
        final DefaultExceptionLogger defaultExceptionLogger = spy(new DefaultExceptionLogger());
        HystrixPlugins.getInstance().registerCommandExecutionHook(new ExceptionLoggingCommandHook(defaultExceptionLogger));
        when(mockAuthenticator.authenticate(any(String.class))).thenThrow(new AuthenticationException("test"));
        doCallRealMethod().when(defaultExceptionLogger).log(any(Exception.class), any(HystrixCommand.class));

        try {
            tenacityAuthenticator.authenticate("foo");
        } catch (HystrixRuntimeException err) {
            assertFalse(Iterables.isEmpty(
                    Iterables.filter(Throwables.getCausalChain(err), AuthenticationException.class)));
        }

        verify(mockAuthenticator, times(1)).authenticate(any(String.class));
        verify(defaultExceptionLogger, times(1)).log(any(Exception.class), any(HystrixCommand.class));
    }

    @Test
    public void shouldNotTransformAuthenticationExceptionIntoMappedException() throws AuthenticationException {
        when(AuthenticatorApp.getMockAuthenticator().authenticate(any(String.class))).thenThrow(new AuthenticationException("test"));
        final Client client = new JerseyClientBuilder(new MetricRegistry())
                .using(executorService, Jackson.newObjectMapper())
                .build("dropwizard-app-rule");

        final Response response = client
                .target(URI.create("http://localhost:" + RULE.getLocalPort() + "/auth"))
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer stuff")
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        verify(AuthenticatorApp.getMockAuthenticator(), times(1)).authenticate(any(String.class));
        verifyZeroInteractions(AuthenticatorApp.getTenacityContainerExceptionMapper());
        verify(AuthenticatorApp.getTenacityExceptionMapper(), times(1)).toResponse(any(HystrixRuntimeException.class));
    }

    public static class AuthenticatorApp extends Application<Configuration> {
        private static Authenticator<String, Object> mockAuthenticator;
        private static Authenticator<String, Object> tenacityAuthenticator;
        private static TenacityExceptionMapper tenacityExceptionMapper = spy(new TenacityExceptionMapper());
        private static TenacityContainerExceptionMapper tenacityContainerExceptionMapper =
                spy(new TenacityContainerExceptionMapper());

        public static void main(String[] args) throws Exception {
            new AuthenticatorApp().run(args);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            mockAuthenticator = mock(Authenticator.class);
            tenacityAuthenticator = TenacityAuthenticator.wrap(mockAuthenticator, DependencyKey.TENACITY_AUTH_TIMEOUT);
            environment.jersey().register(AuthFactory.binder(new OAuthFactory<>(tenacityAuthenticator, "test-realm", Object.class)));
            environment.jersey().register(tenacityExceptionMapper);
            environment.jersey().register(tenacityContainerExceptionMapper);
            environment.jersey().register(new AuthErrorResource());
        }

        public static Authenticator<String, Object> getMockAuthenticator() {
            return mockAuthenticator;
        }

        public static Authenticator<String, Object> getTenacityAuthenticator() {
            return tenacityAuthenticator;
        }

        public static TenacityExceptionMapper getTenacityExceptionMapper() {
            return tenacityExceptionMapper;
        }

        public static TenacityContainerExceptionMapper getTenacityContainerExceptionMapper() {
            return tenacityContainerExceptionMapper;
        }
    }

    @Path("/auth")
    public static class AuthErrorResource {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Response alwaysThrow(@Auth Object principal) throws AuthenticationException {
            throw new IllegalStateException("Should never reach this code");
        }
    }
}