package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.yammer.tenacity.core.auth.TenacityAuthenticator;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.security.Principal;

import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TenacityContainerExceptionMapperTest {
    @Rule
    public TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @SuppressWarnings("unchecked")
    private Authenticator<String, Principal> mockAuthenticator = mock(Authenticator.class);
    private final int statusCode = 429;

    @Path("/")
    public static class FakeResource {
        public FakeResource() {
        }

        @GET
        public Response fakeGet(@Auth Principal principal) {
            return Response.ok().build();
        }

        @POST
        public Response fakePost() {
            return Response.ok().build();
        }
    }

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new FakeResource())
            .addProvider(new AuthDynamicFeature(
                    new OAuthCredentialAuthFilter.Builder<>()
                            .setAuthenticator(TenacityAuthenticator.wrap(
                                    mockAuthenticator, DependencyKey.TENACITY_AUTH_TIMEOUT))
                            .setPrefix("Bearer")
                            .buildAuthFilter()))
            .addProvider(new TenacityContainerExceptionMapper(statusCode))
            .addProvider(new TenacityExceptionMapper(statusCode))
            .build();

    @Test(expected = InternalServerErrorException.class)
    public void exceptionsShouldNotMap() throws AuthenticationException {
        when(mockAuthenticator.authenticate(anyString())).thenThrow(new RuntimeException());
        resources.client()
                .target("/")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                .get(String.class);
    }

    @Test
    public void exceptionsShouldMapTimeouts() throws AuthenticationException {
        Optional<Integer> responseStatus;
        try {
            final TenacityConfiguration timeoutConfiguration = new TenacityConfiguration();
            timeoutConfiguration.setExecutionIsolationThreadTimeoutInMillis(1);
            new TenacityPropertyRegister(
                    ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.TENACITY_AUTH_TIMEOUT, timeoutConfiguration),
                    new BreakerboxConfiguration())
                    .register();

            when(mockAuthenticator.authenticate(anyString())).thenAnswer(new Answer<Optional<Principal>>() {
                @Override
                public Optional<Principal> answer(InvocationOnMock invocation) throws Throwable {
                    Thread.sleep(100);
                    return Optional.absent();
                }
            });
            final Response response = resources.client()
                    .target("/")
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                    .get(Response.class);
            responseStatus = Optional.of(response.getStatus());
        } catch (ResponseProcessingException err) {
            responseStatus = Optional.of(err.getResponse().getStatus());
        }
        assertThat(responseStatus).contains(statusCode);
    }

    @Test(expected = InternalServerErrorException.class)
    public void authenticationExceptions() throws AuthenticationException {
        when(mockAuthenticator.authenticate(anyString())).thenThrow(new AuthenticationException("auth error"));
        resources.client()
                .target("/")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                .get(String.class);
    }
}
