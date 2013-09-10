package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.tenacity.core.healthchecks.TenacityCircuitBreakersHealthcheck;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.testing.TenacityTest;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class TenacityCircuitBreakersHealthcheckTest extends TenacityTest {
    @Test
    public void healthyWithNoCircuitBreakers() {
        final TenacityCircuitBreakersHealthcheck healthcheck =
                new TenacityCircuitBreakersHealthcheck(ImmutableList.<TenacityPropertyKey>of());

        assertThat(healthcheck.execute()).isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void healthyWithNonExistentCircuitBreakers() {
        final TenacityCircuitBreakersHealthcheck healthcheck =
                new TenacityCircuitBreakersHealthcheck(ImmutableList.<TenacityPropertyKey>of(DependencyKey.NON_EXISTENT_HEALTHCHECK));

        assertThat(healthcheck.execute()).isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void healthyWithClosedCircuitBreakers() {
        final TenacityCircuitBreakersHealthcheck healthcheck =
                new TenacityCircuitBreakersHealthcheck(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK));

        new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute();

        assertThat(healthcheck.execute()).isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void healthyExistentAgnostic() {
        final TenacityCircuitBreakersHealthcheck healthcheck =
                new TenacityCircuitBreakersHealthcheck(ImmutableList.<TenacityPropertyKey>of(DependencyKey.NON_EXISTENT_HEALTHCHECK, DependencyKey.EXISTENT_HEALTHCHECK));

        new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute();

        assertThat(healthcheck.execute()).isEqualTo(HealthCheck.Result.healthy());
    }

    private static void tryToOpenCircuitBreaker(TenacityPropertyKey key) {
        for (int i = 0; i < 100; i++) {
            new TenacityFailingCommand(key).execute();
        }
    }

    @Test
    public void unhealthyWithOpenCircuitBreaker() {
        final TenacityCircuitBreakersHealthcheck healthcheck =
                new TenacityCircuitBreakersHealthcheck(ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXISTENT_HEALTHCHECK));

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);

        assertThat(healthcheck.execute())
                .isEqualTo(HealthCheck.Result.unhealthy(ImmutableList.of(DependencyKey.EXISTENT_HEALTHCHECK).toString()));
    }

    @Test
    public void mixedResults() {
        final TenacityCircuitBreakersHealthcheck healthcheck =
                new TenacityCircuitBreakersHealthcheck(ImmutableList.<TenacityPropertyKey>of(
                        DependencyKey.EXISTENT_HEALTHCHECK, DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK));

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);

        assertThat(healthcheck.execute())
                .isEqualTo(HealthCheck.Result.unhealthy(ImmutableList.of(DependencyKey.EXISTENT_HEALTHCHECK).toString()));
    }

    @Test
    public void multipleOpen() {
        final TenacityCircuitBreakersHealthcheck healthcheck =
                new TenacityCircuitBreakersHealthcheck(ImmutableList.<TenacityPropertyKey>of(
                        DependencyKey.EXISTENT_HEALTHCHECK, DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK));

        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);
        tryToOpenCircuitBreaker(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK);

        assertThat(healthcheck.execute())
                .isEqualTo(HealthCheck.Result.unhealthy(
                        ImmutableList.of(DependencyKey.EXISTENT_HEALTHCHECK, DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK).toString()));
    }
}