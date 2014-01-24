package com.yammer.tenacity.tests;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicCredentials;
import com.yammer.dropwizard.auth.oauth.OAuthProvider;
import com.yammer.dropwizard.testing.ResourceTest;
import com.yammer.tenacity.core.auth.TenacityAuthenticator;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TenacityContainerExceptionMapperTest extends ResourceTest {
    @SuppressWarnings("unchecked")
    private Authenticator<String, BasicCredentials> mockAuthenticator = mock(Authenticator.class);
    private final int statusCode = 429;

    @Path("/")
    private static class FakeResource {
        public FakeResource() {
        }

        @GET
        @SuppressWarnings("unused")
        public Response fakeGet(@Auth BasicCredentials basicCredentials) {
            return Response.ok().build();
        }
    }

    @Override
    protected void setUpResources() throws Exception {
        addResource(new FakeResource());
        addProvider(new OAuthProvider<>(TenacityAuthenticator
                .wrap(mockAuthenticator, DependencyKey.TENACITY_AUTH_TIMEOUT), "auth"));
        addProvider(new TenacityContainerExceptionMapper(statusCode));
    }

    @Test
    public void exceptionsShouldMap() throws AuthenticationException {
        try {
            when(mockAuthenticator.authenticate(anyString())).thenThrow(new RuntimeException());
            client()
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
            client()
                    .resource("/")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer TEST")
                    .get(String.class);
        } catch (UniformInterfaceException err) {
            assertThat(err.getResponse().getStatus()).isEqualTo(statusCode);
        }
    }
}
