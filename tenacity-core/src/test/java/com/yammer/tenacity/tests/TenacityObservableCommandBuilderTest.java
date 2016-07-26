package com.yammer.tenacity.tests;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.TenacityObservableCommand;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;
import rx.Observable;

import static org.assertj.core.api.Assertions.assertThat;

public class TenacityObservableCommandBuilderTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test(expected = IllegalStateException.class)
    public void noRunNoFallbackShouldFail() {
        TenacityObservableCommand
                .builder(DependencyKey.GENERAL)
                .observe();
    }

    @Test
    public void simpleBuildNoFallback() {
        assertThat(TenacityObservableCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> Observable.just(1))
                .build()
                .observe().toBlocking().single()).isEqualTo(1);
    }

    @Test
    public void simpleObserveNoFallback() {
        assertThat(TenacityCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> 1)
                .observe().toBlocking().single()).isEqualTo(1);
    }

    @Test(expected = HystrixRuntimeException.class)
    public void commandThatAlwaysShouldCallFallbackAndItDoesntExist() {
        assertThat(TenacityObservableCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> {throw new RuntimeException();})
                .observe().toBlocking().single());
    }

    @Test
    public void commandThatAlwaysShouldCallFallbackAndItShouldSucceed() {
        assertThat(TenacityObservableCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> {throw new RuntimeException();})
                .fallback(() -> Observable.just(2))
                .observe().toBlocking().single()).isEqualTo(2);
    }
}
