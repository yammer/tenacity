package com.yammer.tenacity.core.tests;

import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.TenacityObservableCommand;
import com.yammer.tenacity.core.core.TenacityObservables;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.testing.TenacityTestRule;
import com.yammer.tenacity.tests.DependencyKey;
import io.dropwizard.util.Duration;
import org.junit.Rule;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class TenacityObservablesTest {
    private static class TestCommand<ReturnType> extends TenacityObservableCommand<ReturnType> {
        private Callable<ReturnType> calllable;

        public TestCommand(TenacityPropertyKey tenacityPropertyKey, Callable<ReturnType> calllable) {
            super(tenacityPropertyKey);
            this.calllable = calllable;
        }

        @Override
        protected Observable<ReturnType> construct() {
            return Observable.create(new Observable.OnSubscribe<ReturnType>() {
                @Override
                public void call(Subscriber<? super ReturnType> subscriber) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onStart();
                        try {
                            subscriber.onNext(calllable.call());
                            subscriber.onCompleted();
                        } catch (Exception err) {
                            subscriber.onError(err);
                        }
                    }
                }
            });
        }
    }

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void primaryOnlyExecutes() {
        assertThat(TenacityObservables.execute(
                new TestCommand<>(DependencyKey.GENERAL, new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return 1;
                    }
                }),
                Observable.<Integer>error(new IllegalStateException())))
                .isEqualTo(1);

        assertThat(TenacityObservables.execute(
                new TenacityCommand<Integer>(DependencyKey.GENERAL) {
                    @Override
                    protected Integer run() throws Exception {
                        return 1;
                    }
                },
                Observable.<Integer>error(new IllegalStateException())))
                .isEqualTo(1);
    }

    @Test(expected = IllegalStateException.class)
    public void bothErrorsThrowsLastException() {
        assertThat(TenacityObservables.execute(
                Observable.<Integer>error(new IllegalArgumentException()),
                Observable.<Integer>error(new IllegalStateException())))
                .isEqualTo(1);
    }

    @Test
    public void shouldSilentlyAttemptSecondObservable() {
        assertThat(TenacityObservables.execute(
                Observable.<Integer>error(new IllegalArgumentException()),
                new TestCommand<>(DependencyKey.GENERAL, new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return 1;
                    }
                })))
                .isEqualTo(1);

        assertThat(TenacityObservables.execute(
                Observable.<Integer>error(new IllegalArgumentException()),
                new TenacityCommand<Integer>(DependencyKey.GENERAL) {
                    @Override
                    protected Integer run() throws Exception {
                        return 1;
                    }
                }))
                .isEqualTo(1);
    }

    @Test
    public void shouldAttemptSecondAfterTimeoutInFirst() {
        assertTrue(TenacityObservables.execute(
                new TimeoutObservable(Duration.seconds(3)),
                new TestCommand<>(DependencyKey.GENERAL, new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                })));

        assertTrue(TenacityObservables.execute(
                new TimeoutObservable(Duration.seconds(3)),
                new TenacityCommand<Boolean>(DependencyKey.GENERAL) {
                    @Override
                    protected Boolean run() throws Exception {
                        return true;
                    }
                }));
    }

    @Test
    public void shouldRespectFallbacks() {
        assertTrue(TenacityObservables.execute(
                new TenacityObservableCommand<Boolean>(DependencyKey.GENERAL) {
                    @Override
                    protected Observable<Boolean> construct() {
                        return Observable.error(new IllegalStateException());
                    }

                    @Override
                    protected Observable<Boolean> resumeWithFallback() {
                        return Observable.just(true);
                    }
                },
                Observable.<Boolean>error(new IllegalStateException())));

        assertTrue(TenacityObservables.execute(
                new TenacityCommand<Boolean>(DependencyKey.GENERAL) {
                    @Override
                    protected Boolean run() throws Exception {
                        throw new IllegalStateException();
                    }

                    @Override
                    protected Boolean getFallback() {
                        return true;
                    }
                },
                Observable.<Boolean>error(new IllegalStateException())));
    }
}