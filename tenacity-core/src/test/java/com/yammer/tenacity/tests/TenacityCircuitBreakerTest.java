package com.yammer.tenacity.tests;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.yammer.tenacity.core.TenacityPropertyStore;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TenacityCircuitBreakerTest extends TenacityTest {
    private final TenacityPropertyStore tenacityPropertyStore = new TenacityPropertyStore();
    @Test
    public void circuitBreakerShouldOpen() throws URISyntaxException, InterruptedException {
        for (int i = 0; i < 500; i++) {
            Thread.sleep(1);
            new TenacityFailingCommand(tenacityPropertyStore).execute();
        }

        final HystrixCommandMetrics sleepCommandMetrics = HystrixCommandMetrics
                .getInstance(new TenacityFailingCommand(tenacityPropertyStore).getCommandKey());
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT))
                .isEqualTo(0);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isEqualTo(500);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED))
                .isGreaterThan(50);
        assertFalse("Allow request should be false", HystrixCircuitBreaker.Factory.getInstance(new TenacityFailingCommand(tenacityPropertyStore).getCommandKey()).allowRequest());
        assertTrue("Circuit Breaker should be open", new TenacityFailingCommand(tenacityPropertyStore).isCircuitBreakerOpen());
    }

    @Test
    public void circuitBreakerShouldBeClosed() throws URISyntaxException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            final TenacityFailingCommand command = new TenacityFailingCommand(tenacityPropertyStore);
            command.execute();
            assertTrue("Allow request should be true", HystrixCircuitBreaker.Factory.getInstance(new TenacityFailingCommand(tenacityPropertyStore).getCommandKey()).allowRequest());
            assertFalse("Circuit Breaker should not be open", new TenacityFailingCommand(tenacityPropertyStore).isCircuitBreakerOpen());
        }

        final HystrixCommandMetrics sleepCommandMetrics = HystrixCommandMetrics
                .getInstance(new TenacityFailingCommand(tenacityPropertyStore).getCommandKey());
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT))
                .isEqualTo(0);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isEqualTo(10);
        assertThat(sleepCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.SHORT_CIRCUITED))
                .isEqualTo(0);
        assertTrue("Allow request should be true", HystrixCircuitBreaker.Factory.getInstance(new TenacityFailingCommand(tenacityPropertyStore).getCommandKey()).allowRequest());
        assertFalse("Circuit Breaker should not be open", new TenacityFailingCommand(tenacityPropertyStore).isCircuitBreakerOpen());
    }
}