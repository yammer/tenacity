package com.yammer.tenacity.tests;

import com.sun.jersey.api.client.UniformInterfaceException;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.oauth.OAuthProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import com.yammer.tenacity.core.auth.TenacityAuthenticator;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import org.junit.Rule;
import org.junit.Test;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TenacityContainerExceptionMapperTest {
    @SuppressWarnings("unchecked")
    private Authenticator<String, BasicCredentials> mockAuthenticator = mock(Authenticator.class);
    private final int statusCode = 429;

    @Path("/")
    private static class FakeResource {
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
            .addProvider(new OAuthProvider<>(TenacityAuthenticator
                .wrap(mockAuthenticator, DependencyKey.TENACITY_AUTH_TIMEOUT), "auth"))
            .addProvider(new TenacityContainerExceptionMapper(statusCode))
            .build();

    @Test
    public void exceptionsShouldMap() throws AuthenticationException {
        try {
            when(mockAuthenticator.authenticate(anyString())).thenThrow(new RuntimeException());
            resources.client()
                    .resource("/")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                    .get(String.class);
        } catch (UniformInterfaceException err) {
            assertThat(err.getResponse().getStatus()).isEqualTo(statusCode);
        }
    }

    @Test
    public void authenticationExceptions() throws AuthenticationException {
        try {
            when(mockAuthenticator.authenticate(anyString())).thenThrow(new AuthenticationException("auth error"));
            resources.client()
                    .resource("/")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                    .get(String.class);
        } catch (UniformInterfaceException err) {
            assertThat(err.getResponse().getStatus()).isEqualTo(statusCode);
        }
    }
}
