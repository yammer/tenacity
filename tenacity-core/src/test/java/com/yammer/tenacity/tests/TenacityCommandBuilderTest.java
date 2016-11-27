package com.yammer.tenacity.tests;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TenacityCommandBuilderTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test(expected = IllegalStateException.class)
    public void noRunNoFallbackShouldFail() {
        TenacityCommand
                .builder(DependencyKey.GENERAL)
                .execute();
    }

    @Test
    public void simpleBuildNoFallback() {
        assertThat(TenacityCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> 1)
                .build()
                .execute()).isEqualTo(1);
    }

    @Test
    public void simpleExecuteNoFallback() {
        assertThat(TenacityCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> 1)
                .execute()).isEqualTo(1);
    }

    @Test
    public void simpleQueueNoFallback() throws Exception {
        assertThat(TenacityCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> 1)
                .queue().get()).isEqualTo(1);
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
        TenacityCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> {throw new RuntimeException();})
                .execute();
    }

    @Test
    public void commandThatAlwaysShouldCallFallbackAndItShouldSucceed() {
        assertThat(TenacityCommand
                .builder(DependencyKey.GENERAL)
                .run(() -> {throw new RuntimeException();})
                .fallback(() -> 2)
                .execute()).isEqualTo(2);
    }
}
