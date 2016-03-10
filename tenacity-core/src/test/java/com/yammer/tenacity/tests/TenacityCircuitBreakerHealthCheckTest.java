package com.yammer.tenacity.tests;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.core.CircuitBreakers;
import com.yammer.tenacity.core.healthcheck.TenacityCircuitBreakerHealthCheck;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.util.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TenacityCircuitBreakerHealthCheckTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Before
    public void setup() {
        TenacityPropertyRegister.setDefaultMetricsHealthSnapshotInterval(Duration.milliseconds(10));
    }

    @Test
    public void healthyWhenNoCircuitBreakers() {
        assertThat(new TenacityCircuitBreakerHealthCheck().execute())
                .isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void healthyWhenThereIsNoOpenCircuitBreakers() {
        final HealthCheck healthCheck = new TenacityCircuitBreakerHealthCheck(DependencyKey.EXISTENT_HEALTHCHECK);

        assertThat(CircuitBreakers.all(DependencyKey.EXISTENT_HEALTHCHECK)).isEmpty();
        new TenacitySuccessCommand(DependencyKey.EXISTENT_HEALTHCHECK).execute();
        assertThat(CircuitBreakers.all(DependencyKey.EXISTENT_HEALTHCHECK))
                .contains(CircuitBreaker.closed(DependencyKey.EXISTENT_HEALTHCHECK));

        assertThat(healthCheck.execute())
                .isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void unhealthyWhenThereIsAnOpenCircuitBreaker() {
        final HealthCheck healthCheck = new TenacityCircuitBreakerHealthCheck(DependencyKey.EXISTENT_HEALTHCHECK);

        assertThat(CircuitBreakers.all(DependencyKey.EXISTENT_HEALTHCHECK)).isEmpty();
        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);
        assertThat(CircuitBreakers.all(DependencyKey.EXISTENT_HEALTHCHECK))
                .contains(CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK));

        assertThat(healthCheck.execute())
                .isEqualToComparingOnlyGivenFields(HealthCheck.Result.unhealthy(""), "healthy");
    }

    @Test
    public void multipleUnhealthyWhenThereIsAnOpenCircuitBreaker() {
        final ImmutableList<TenacityPropertyKey> keys = ImmutableList.<TenacityPropertyKey>of(
                DependencyKey.EXISTENT_HEALTHCHECK, DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK);
        final HealthCheck healthCheck = new TenacityCircuitBreakerHealthCheck(keys);

        assertThat(CircuitBreakers.all(keys)).isEmpty();
        tryToOpenCircuitBreaker(DependencyKey.EXISTENT_HEALTHCHECK);
        tryToOpenCircuitBreaker(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK);
        assertThat(CircuitBreakers.all(keys))
                .contains(CircuitBreaker.open(DependencyKey.EXISTENT_HEALTHCHECK),
                        CircuitBreaker.open(DependencyKey.ANOTHER_EXISTENT_HEALTHCHECK));

        assertThat(healthCheck.execute())
                .isEqualToComparingOnlyGivenFields(HealthCheck.Result.unhealthy(""), "healthy");
    }

    private static void tryToOpenCircuitBreaker(TenacityPropertyKey key) {
        for (int i = 0; i < 100; i++) {
            new TenacityFailingCommand(key).execute();
        }
    }
}