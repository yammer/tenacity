package com.yammer.tenacity.tests;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TenacityExceptionMapperTest {
    @Path("/{key}")
    public static class FakeResource {
        private final TenacityPropertyKeyFactory factory;

        public FakeResource(TenacityPropertyKeyFactory factory) {
            this.factory = factory;
        }

        @GET
        public Response fakeGet(@PathParam("key") String key) {
            return Response.ok(factory.from(key)).build();
        }
    }

    private TenacityPropertyKeyFactory mockFactory = mock(TenacityPropertyKeyFactory.class);
    private final int statusCode = 429; // Too Many Requests http://tools.ietf.org/html/rfc6585#section-4

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(new TenacityExceptionMapper(statusCode))
            .addResource(new FakeResource(mockFactory))
            .build();

    private void setupFailureType(HystrixRuntimeException.FailureType failureType) {
        when(mockFactory.from(anyString())).thenThrow(
                new HystrixRuntimeException(
                        failureType,
                        TenacityCommand.class,
                        "test failure",
                        new TimeoutException(),
                        new TimeoutException())
        );
    }

    @Test
    public void shouldReturnThrottleCodeOnUncaughtTenacityException() {
        setupFailureType(HystrixRuntimeException.FailureType.TIMEOUT);

        try {
            resources.client()
                    .target("/random")
                    .request()
                    .get(String.class);
        } catch (HystrixRuntimeException err) {
            fail("Should not have thrown HystrixRuntimeException");
        } catch (ClientErrorException err) {
            assertThat(err.getResponse().getStatus(),
                    is(equalTo(statusCode)));
        }

        verify(mockFactory, times(1)).from(anyString());
    }

    @Test
    public void shouldStillThrowServerError() {
        setupFailureType(HystrixRuntimeException.FailureType.COMMAND_EXCEPTION);

        try {
            resources.client()
                    .target("/random")
                    .request()
                    .get(String.class);
        } catch (InternalServerErrorException err) {
            assertThat(err.getResponse().getStatus(),
                    is(equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())));
        }

        verify(mockFactory, times(1)).from(anyString());
    }
}
