package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.container.ContainerException;
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
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TenacityAuthenticatorTest {

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    private Authenticator<String, Object> mockAuthenticator;
    private Authenticator<String, Object> tenacityAuthenticator;
    private TenacityExceptionMapper tenacityExceptionMapper = spy(new TenacityExceptionMapper());
    private TenacityContainerExceptionMapper tenacityContainerExceptionMapper =
            spy(new TenacityContainerExceptionMapper());
    @Rule
    public final ResourceTestRule resources;

    {
        mockAuthenticator = mock(Authenticator.class);
        tenacityAuthenticator = TenacityAuthenticator.wrap(mockAuthenticator, DependencyKey.TENACITY_AUTH_TIMEOUT);
        resources = ResourceTestRule.builder()
            .addResource(new AuthResource())
            .addProvider(new OAuthProvider<>(tenacityAuthenticator, "test-realm"))
            .addProvider(tenacityExceptionMapper)
            .addProvider(tenacityContainerExceptionMapper)
            .build();
    }

    @Before
    public void setup() {
        reset(mockAuthenticator);
        reset(tenacityExceptionMapper);
        reset(tenacityContainerExceptionMapper);
    }

    @Path("/auth")
    private static class AuthResource {
        @GET @Produces(MediaType.TEXT_PLAIN)
        public Response alwaysThrow(@Auth String auth) {
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
                .isEqualTo(Optional.absent());
        } catch (HystrixRuntimeException err) {
            assertThat(err.getFailureType()).isEqualTo(HystrixRuntimeException.FailureType.TIMEOUT);
            throw err;
        }
    }

    @Test @Ignore("Can't run this until we can reset the HystrixCommandExecutionHook")
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
        when(mockAuthenticator.authenticate(any(String.class))).thenThrow(new AuthenticationException("test"));
        final ClientResponse response = resources
                .client()
                .resource("/auth")
                .header(HttpHeaders.AUTHORIZATION, "Bearer stuff")
                .get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        verify(mockAuthenticator, times(1)).authenticate(any(String.class));
        verify(tenacityContainerExceptionMapper, times(1)).toResponse(any(ContainerException.class));
        verify(tenacityExceptionMapper, never()).toResponse(any(HystrixRuntimeException.class));
    }
}