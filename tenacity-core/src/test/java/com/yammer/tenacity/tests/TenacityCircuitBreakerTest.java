package com.yammer.tenacity.tests;

import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TenacityCircuitBreakerTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void circuitBreakerShouldOpen() throws URISyntaxException, InterruptedException {
        final TenacityFailingCommand tenacityFailingCommand = new TenacityFailingCommand();
        tenacityFailingCommand
                .getCumulativeCommandEventCounterStream()
                .startCachingStreamValuesIfUnstarted();
        final int numberOfExecutions = 500;

        for (int i = 0; i < numberOfExecutions; i++) {
            new TenacityFailingCommand().execute();

            //Allow for circuit breaker calculations to take place and open the circuit
            if (i == (numberOfExecutions / 2)) {
                Thread.sleep(500);
            }
        }

        Thread.sleep(1000);

        final HystrixCommandMetrics sleepCommandMetrics = tenacityFailingCommand.getCommandMetrics();
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT))
                .isZero();
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isEqualTo(numberOfExecutions);
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED))
                .isGreaterThan(50L);
        assertFalse("Allow request should be false", tenacityFailingCommand.getCircuitBreaker().allowRequest());
        assertTrue("Circuit Breaker should be open", tenacityFailingCommand.isCircuitBreakerOpen());
    }

    @Test
    public void circuitBreakerShouldBeClosed() throws URISyntaxException, InterruptedException {
        final TenacityFailingCommand tenacityFailingCommand = new TenacityFailingCommand();
        tenacityFailingCommand
                .getCumulativeCommandEventCounterStream()
                .startCachingStreamValuesIfUnstarted();
        final int numberOfExecutions = 10;

        for (int i = 0; i < numberOfExecutions; i++) {
            new TenacityFailingCommand().execute();
            assertTrue("Allow request should be true", tenacityFailingCommand.getCircuitBreaker().allowRequest());
            assertFalse("Circuit Breaker should not be open", tenacityFailingCommand.isCircuitBreakerOpen());
        }

        Thread.sleep(1000);

        final HystrixCommandMetrics sleepCommandMetrics = tenacityFailingCommand.getCommandMetrics();
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT))
                .isEqualTo(0L);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isEqualTo(10L);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED))
                .isEqualTo(0L);
        assertTrue("Allow request should be true", tenacityFailingCommand.getCircuitBreaker().allowRequest());
        assertFalse("Circuit Breaker should not be open", tenacityFailingCommand.isCircuitBreakerOpen());
    }
}