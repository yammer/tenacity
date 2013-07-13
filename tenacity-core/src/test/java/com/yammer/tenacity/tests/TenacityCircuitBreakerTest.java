package com.yammer.tenacity.tests;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.yammer.tenacity.core.TenacityPropertyStore;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class TenacityCircuitBreakerTest extends TenacityTest {
    private final TenacityPropertyStore tenacityPropertyStore = new TenacityPropertyStore();
    @Test
    public void circuitBreakerShouldOpen() throws URISyntaxException, InterruptedException {
        for (int i = 0; i < 500; i++) {
            Thread.sleep(1);
            try {
                new TenacityFailingCommand(tenacityPropertyStore).execute();
            } catch (Exception err) {
                fail();
            }
        }
        assertFalse("Allow request should be false", HystrixCircuitBreaker.Factory.getInstance(HystrixCommandKey.Factory.asKey("Failing")).allowRequest());
        assertTrue("Circuit Breaker should be open", new TenacityFailingCommand(tenacityPropertyStore).isCircuitBreakerOpen());
    }

    @Test
    public void circuitBreakerShouldBeClosed() throws URISyntaxException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            try {
                final TenacityFailingCommand command = new TenacityFailingCommand(tenacityPropertyStore);
                command.execute();
                assertTrue("Allow request should be true", HystrixCircuitBreaker.Factory.getInstance(HystrixCommandKey.Factory.asKey("Failing")).allowRequest());
                assertFalse("Circuit Breaker should not be open", new TenacityFailingCommand(tenacityPropertyStore).isCircuitBreakerOpen());
            } catch (Exception err) {
                fail();
            }
        }
    }
}
