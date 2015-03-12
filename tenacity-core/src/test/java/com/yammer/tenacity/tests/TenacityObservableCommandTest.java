package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.yammer.tenacity.core.TenacityObservableCommand;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.util.Duration;
import org.junit.Rule;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.*;

public class TenacityObservableCommandTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    private static void executeTimeoutAndVerify(final TenacityObservableCommand<Boolean> timeoutCommand) {
        try {
            assertTrue(timeoutCommand.observe().toBlocking().single());
        } catch (HystrixRuntimeException err) {
            assertEquals(err.getFailureType(), HystrixRuntimeException.FailureType.TIMEOUT);
        }

        assertEquals(timeoutCommand.isResponseTimedOut(), true);
        assertEquals(timeoutCommand.getMetrics().getCumulativeCount(HystrixRollingNumberEvent.TIMEOUT), 1);
    }

    @Test
    public void shouldTimeout() {
        executeTimeoutAndVerify(new TimeoutObservableCommand(Duration.milliseconds(1500)));
    }

    @Test
    public void shouldTimeoutAndRespectsKeyProperties() {
        final TenacityConfiguration tenacityConfiguration = new TenacityConfiguration();
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(100);

        new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.OBSERVABLE_TIMEOUT, tenacityConfiguration),
                new BreakerboxConfiguration())
                .register();

        executeTimeoutAndVerify(new TimeoutObservableCommand(Duration.milliseconds(300)));
    }

    @Test
    public void shouldNotTimeout() {
        final TenacityObservableCommand<Boolean> command = new TimeoutObservableCommand(Duration.milliseconds(100));
        assertTrue(command.toObservable().toBlocking().single());
    }

    @Test
    public void shouldNotTimeoutAndRespectsProperties() {
        final TenacityConfiguration tenacityConfiguration = new TenacityConfiguration();
        tenacityConfiguration.setExecutionIsolationThreadTimeoutInMillis(2000);

        new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.OBSERVABLE_TIMEOUT, tenacityConfiguration),
                new BreakerboxConfiguration())
                .register();
        final TenacityObservableCommand<Boolean> command = new TimeoutObservableCommand(Duration.milliseconds(1250));
        assertTrue(command.toObservable().toBlocking().single());
    }

    private static class TimeoutObservableCommand extends TenacityObservableCommand<Boolean> {
        private final Duration sleepDuration;

        public TimeoutObservableCommand(Duration sleepDuration) {
            super(DependencyKey.OBSERVABLE_TIMEOUT);
            this.sleepDuration = sleepDuration;
        }

        @Override
        protected Observable<Boolean> construct() {
            return Observable.create(new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(Subscriber<? super Boolean> subscriber) {
                    try {
                        Thread.sleep(sleepDuration.toMilliseconds());
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    } catch (InterruptedException err) {
                        subscriber.onError(err);
                        fail("Interrupted observable timeout");
                    }
                }
            }).subscribeOn(Schedulers.computation());
        }
    }
}
