package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TenacityCircuitBreakersResourceTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @SuppressWarnings("unchecked")
    private static final Iterable<TenacityPropertyKey> keysMock = mock(Iterable.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TenacityCircuitBreakersResource(keysMock, new DependencyKeyFactory()))
            .build();

    @Before @SuppressWarnings("unchecked")
    public void setup() {
        reset(keysMock);
    }

    @Test
    public void healthyWithNoCircuitBreakers() {
        when(keysMock.iterator()).thenReturn(Collections.<TenacityPropertyKey>emptyIterator());

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .get(CircuitBreaker[].class)).isEmpty();
    }

    @Test
    public void notFoundWhenNoCircuitBreakers() {
        when(keysMock.iterator()).thenReturn(Collections.<TenacityPropertyKey>emptyIterator());

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .path(DependencyKey.NON_EXISTENT_HEALTHCHECK.name())
                .get(ClientResponse.class).getStatus())
                .isEqualTo(ClientResponse.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void healthyWithNonExistentCircuitBreakers() {
        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.NON_EXISTENT_HEALTHCHECK).iterator());

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .get(CircuitBreaker[].class)).isEmpty();
    }

    @Test
    public void healthyWithClosedCircuitBreakers() {
        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK).iterator());

        new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute();

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .get(CircuitBreaker[].class))
                .containsExactly(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));
    }


    @Test
    public void healthyExistentAgnostic() {
        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.NON_EXISTENT_HEALTHCHECK, DependencyKey.EXISTENT_HEALTHCHECK).iterator());

        new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute();

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .get(CircuitBreaker[].class))
                .containsExactly(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    private static void tryToOpenCircuitBreaker(TenacityPropertyKey key) {
        for (int i = 0; i < 100; i++) {
            new TenacityFailingCommand(key).execute();
        }
    }

    @Test
    public void unhealthyWithOpenCircuitBreaker() {
        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK).iterator());

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .get(CircuitBreaker[].class))
                .containsExactly(CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    @Test
    public void mixedResults() {
        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.NON_EXISTENT_HEALTHCHECK, DependencyKey.EXISTENT_HEALTHCHECK).iterator());

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .get(CircuitBreaker[].class))
                .containsExactly(CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    @Test
    public void multipleOpen() {
        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK, DependencyKey.EXISTENT_HEALTHCHECK).iterator());

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);
        tryToOpenCircuitBreaker(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK);

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .get(CircuitBreaker[].class))
                .contains(
                        CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK),
                        CircuitBreaker.open(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK));
    }

    @Test
    public void canFindACircuitBreaker() {
        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK).iterator());

        new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute();

        assertThat(resources.client().resource(TenacityCircuitBreakersResource.PATH)
                .path(DependencyKey.EXISTENT_HEALTHCHECK.name())
                .get(CircuitBreaker.class))
                .isEqualTo(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    @Test
    public void forcedCloseIsIdentifiedCorrectly() {
        TenacityPropertyRegister.registerCircuitForceClose(DependencyKey.EXISTENT_HEALTHCHECK);

        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK).iterator());

        assertThat(new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute()).isEqualTo("value");

        assertThat(resources.client().target(TenacityCircuitBreakersResource.PATH)
                .path(DependencyKey.EXISTENT_HEALTHCHECK.name())
                .request()
                .get(CircuitBreaker.class))
                .isEqualTo(CircuitBreaker.forcedClosed(DependencyKey.EXISTENT_HEALTHCHECK))
                .isNotEqualTo(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    @Test
    public void forcedOpenIsIdentifiedCorrectly() {
        TenacityPropertyRegister.registerCircuitForceOpen(DependencyKey.EXISTENT_HEALTHCHECK);

        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK).iterator());

        assertThat(new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute()).isEqualTo("fallback");

        assertThat(resources.client().target(TenacityCircuitBreakersResource.PATH)
                .path(DependencyKey.EXISTENT_HEALTHCHECK.name())
                .request()
                .get(CircuitBreaker.class))
                .isEqualTo(CircuitBreaker.forcedOpen(DependencyKey.EXISTENT_HEALTHCHECK))
                .isNotEqualTo(CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    @Test
    public void forcedOpenCanBeReset() {
        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK).iterator());
        assertThat(new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute()).isEqualTo("value");
        assertThat(resources.client().target(TenacityCircuitBreakersResource.PATH)
                .path(DependencyKey.EXISTENT_HEALTHCHECK.name())
                .request()
                .get(CircuitBreaker.class))
                .isEqualTo(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));

        TenacityPropertyRegister.registerCircuitForceOpen(DependencyKey.EXISTENT_HEALTHCHECK);

        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK).iterator());
        assertThat(new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute()).isEqualTo("fallback");
        assertThat(resources.client().target(TenacityCircuitBreakersResource.PATH)
                .path(DependencyKey.EXISTENT_HEALTHCHECK.name())
                .request()
                .get(CircuitBreaker.class))
                .isEqualTo(CircuitBreaker.forcedOpen(DependencyKey.EXISTENT_HEALTHCHECK))
                .isNotEqualTo(CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK));


        TenacityPropertyRegister.registerCircuitForceReset(DependencyKey.EXISTENT_HEALTHCHECK);

        when(keysMock.iterator()).thenReturn(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK).iterator());
        assertThat(new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute()).isEqualTo("value");
        assertThat(resources.client().target(TenacityCircuitBreakersResource.PATH)
                .path(DependencyKey.EXISTENT_HEALTHCHECK.name())
                .request()
                .get(CircuitBreaker.class))
                .isEqualTo(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));
    }
}