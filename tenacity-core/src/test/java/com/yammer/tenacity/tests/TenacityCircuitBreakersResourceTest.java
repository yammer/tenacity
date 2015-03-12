package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TenacityCircuitBreakersResourceTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void healthyWithNoCircuitBreakers() {
        final TenacityCircuitBreakersResource resource =
                new TenacityCircuitBreakersResource(ImmutableList.<TenacityPropertyKey>of());

        assertThat(resource.circuitBreakers()).isEmpty();
    }

    @Test
    public void healthyWithNonExistentCircuitBreakers() {
        final TenacityCircuitBreakersResource resource =
                new TenacityCircuitBreakersResource(ImmutableList.<TenacityPropertyKey>of(DependencyKey.NON_EXISTENT_HEALTHCHECK));

        assertThat(resource.circuitBreakers()).isEmpty();
    }

    @Test
    public void healthyWithClosedCircuitBreakers() {
        final TenacityCircuitBreakersResource resource =
                new TenacityCircuitBreakersResource(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK));

        new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute();

        assertThat(resource.circuitBreakers())
                .containsExactly(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    @Test
    public void healthyExistentAgnostic() {
        final TenacityCircuitBreakersResource resource =
                new TenacityCircuitBreakersResource(ImmutableList.<TenacityPropertyKey>of(DependencyKey.NON_EXISTENT_HEALTHCHECK, DependencyKey.EXISTENT_HEALTHCHECK));

        new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute();

        assertThat(resource.circuitBreakers())
                .containsExactly(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    private static void tryToOpenCircuitBreaker(TenacityPropertyKey key) {
        for (int i = 0; i < 100; i++) {
            new TenacityFailingCommand(key).execute();
        }
    }


    @Test
    public void unhealthyWithOpenCircuitBreaker() {
        final TenacityCircuitBreakersResource resource =
                new TenacityCircuitBreakersResource(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK));

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);

        assertThat(resource.circuitBreakers())
                .containsExactly(CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK));
    }


    @Test
    public void mixedResults() {
        final TenacityCircuitBreakersResource resource =
                new TenacityCircuitBreakersResource(ImmutableList.<TenacityPropertyKey>of(DependencyKey.NON_EXISTENT_HEALTHCHECK, DependencyKey.EXISTENT_HEALTHCHECK));

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);

        assertThat(resource.circuitBreakers())
                .containsExactly(CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK));
    }

    @Test
    public void multipleOpen() {
        final TenacityCircuitBreakersResource resource =
                new TenacityCircuitBreakersResource(ImmutableList.<TenacityPropertyKey>of(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK, DependencyKey.EXISTENT_HEALTHCHECK));

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);
        tryToOpenCircuitBreaker(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK);

        assertThat(resource.circuitBreakers())
                .contains(
                        CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK),
                        CircuitBreaker.open(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK));
    }
}