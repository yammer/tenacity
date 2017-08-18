package com.yammer.tenacity.core.tests;

import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.TenacityObservableCommand;
import com.yammer.tenacity.core.core.TenacityObservables;
import com.yammer.tenacity.testing.TenacityTestRule;
import com.yammer.tenacity.tests.DependencyKey;
import io.dropwizard.util.Duration;
import org.junit.Rule;
import org.junit.Test;
import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TenacityObservablesTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void primaryOnlyExecutes() {
        assertThat(TenacityObservables.execute(
                TenacityCommand.<Integer>builder(DependencyKey.GENERAL).run(() -> 1).lazyObservable(),
                Observable.error(new IllegalStateException())))
                .isEqualTo(1);

        assertThat(TenacityObservables.execute(
                TenacityObservableCommand.<Integer>builder(DependencyKey.GENERAL).run(() -> Observable.just(1)).lazyObservable(),
                Observable.error(new IllegalStateException())))
                .isEqualTo(1);
    }

    @Test(expected = IllegalStateException.class)
    public void bothErrorsThrowsLastException() {
        assertThat(TenacityObservables.<Integer>execute(
                Observable.error(new IllegalArgumentException()),
                Observable.error(new IllegalStateException())))
                .isEqualTo(1);
    }

    @Test
    public void shouldSilentlyAttemptSecondObservable() {
        assertThat(TenacityObservables.execute(
                Observable.error(new IllegalArgumentException()),
                TenacityCommand.<Integer>builder(DependencyKey.GENERAL).run(() -> 1).lazyObservable()))
                .isEqualTo(1);

        assertThat(TenacityObservables.execute(
                Observable.error(new IllegalArgumentException()),
                TenacityObservableCommand.<Integer>builder(DependencyKey.GENERAL).run(() -> Observable.just(1)).lazyObservable()))
                .isEqualTo(1);
    }

    @Test
    public void shouldAttemptSecondAfterTimeoutInFirst() {
        assertTrue(TenacityObservables.execute(
                new TimeoutObservable(Duration.seconds(3)),
                TenacityCommand.<Boolean>builder(DependencyKey.GENERAL).run(() -> true).lazyObservable()));

        assertTrue(TenacityObservables.execute(
                new TimeoutObservable(Duration.seconds(3)),
                TenacityObservableCommand.<Boolean>builder(DependencyKey.GENERAL).run(() -> Observable.just(true)).lazyObservable()));
    }

    @Test
    public void shouldRespectFallbacks() {
        assertTrue(TenacityObservables.execute(
                TenacityObservableCommand.<Boolean>builder(DependencyKey.GENERAL)
                        .run(() -> Observable.error(new IllegalStateException()))
                        .fallback(() -> Observable.just(true))
                        .lazyObservable(),
                Observable.error(new IllegalStateException())));

        assertTrue(TenacityObservables.execute(
                TenacityCommand.<Boolean>builder(DependencyKey.GENERAL)
                        .run(() -> { throw new IllegalStateException(); })
                        .fallback(() -> true)
                        .lazyObservable(),
                Observable.error(new IllegalStateException())));
    }
}