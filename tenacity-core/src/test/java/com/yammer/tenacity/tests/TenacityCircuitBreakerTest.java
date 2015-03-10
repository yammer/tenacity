package com.yammer.tenacity.tests;

import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TenacityCircuitBreakerTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void circuitBreakerShouldOpen() throws URISyntaxException, InterruptedException {
        final TenacityFailingCommand tenacityFailingCommand = new TenacityFailingCommand();
        for (int i = 0; i < 500; i++) {
            new TenacityFailingCommand().execute();
        }

        final HystrixCommandMetrics sleepCommandMetrics = tenacityFailingCommand.getCommandMetrics();
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT),
                is(equalTo((0L)))
        );
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS),
                is(equalTo((500L)))
        );
        assertThat(sleepCommandMetrics
                        .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED),
                is(greaterThan(50L)));
        assertFalse("Allow request should be false", tenacityFailingCommand.getCircuitBreaker().allowRequest());
        assertTrue("Circuit Breaker should be open", tenacityFailingCommand.isCircuitBreakerOpen());
    }

    @Test
    public void circuitBreakerShouldBeClosed() throws URISyntaxException, InterruptedException {
        final TenacityFailingCommand tenacityFailingCommand = new TenacityFailingCommand();
        for (int i = 0; i < 10; i++) {
            new TenacityFailingCommand().execute();
            assertTrue("Allow request should be true", tenacityFailingCommand.getCircuitBreaker().allowRequest());
            assertFalse("Circuit Breaker should not be open", tenacityFailingCommand.isCircuitBreakerOpen());
        }

        final HystrixCommandMetrics sleepCommandMetrics = tenacityFailingCommand.getCommandMetrics();
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT),
                is(equalTo((0L))));
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS),
                is(equalTo((10L))));
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED),
                is(equalTo((0L))));
        assertTrue("Allow request should be true", tenacityFailingCommand.getCircuitBreaker().allowRequest());
        assertFalse("Circuit Breaker should not be open", tenacityFailingCommand.isCircuitBreakerOpen());
    }
}