package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableMap;
import com.yammer.tenacity.core.auth.TenacityAuthenticator;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TenacityContainerExceptionMapperTest {
    @Rule
    public TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @SuppressWarnings("unchecked")
    private Authenticator<String, BasicCredentials> mockAuthenticator = mock(Authenticator.class);
    private final int statusCode = 429;

    @Path("/")
    public static class FakeResource {
        public FakeResource() {
        }

        @GET
        public Response fakeGet(@Auth BasicCredentials basicCredentials) {
            return Response.ok().build();
        }
    }

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new FakeResource())
            .addProvider(new OAuthFactory<>(TenacityAuthenticator
                    .wrap(mockAuthenticator, DependencyKey.TENACITY_AUTH_TIMEOUT), "auth", BasicCredentials.class))
            .addProvider(new TenacityContainerExceptionMapper(statusCode))
            .build();

    @Test
    public void exceptionsShouldNotMap() throws AuthenticationException {
        try {
            when(mockAuthenticator.authenticate(anyString())).thenThrow(new RuntimeException());
            resources.client()
                    .target("/")
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                    .get(String.class);
        } catch (ResponseProcessingException err) {
            assertThat(err.getResponse().getStatus(), is(equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())));
        }
    }

    @Test
    public void exceptionsShouldMapTimeouts() throws AuthenticationException {
        try {
            final TenacityConfiguration timeoutConfiguration = new TenacityConfiguration();
            timeoutConfiguration.setExecutionIsolationThreadTimeoutInMillis(1);
            new TenacityPropertyRegister(
                    ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.TENACITY_AUTH_TIMEOUT, timeoutConfiguration),
                    new BreakerboxConfiguration())
                    .register();

            when(mockAuthenticator.authenticate(anyString())).thenAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    Thread.sleep(100);
                    return null;
                }
            });
            resources.client()
                    .target("/")
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                    .get(String.class);
        } catch (ResponseProcessingException err) {
            assertThat(err.getResponse().getStatus(), is(equalTo(statusCode)));
        }
    }

    @Test
    public void authenticationExceptions() throws AuthenticationException {
        try {
            when(mockAuthenticator.authenticate(anyString())).thenThrow(new AuthenticationException("auth error"));
            resources.client()
                    .target("/")
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                    .get(String.class);
        } catch (ResponseProcessingException err) {
            assertThat(err.getResponse().getStatus(), is(equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())));
        }
    }
}
