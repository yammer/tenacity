package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.bundle.TenacityBundle;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import org.junit.Test;

import javax.ws.rs.ext.ExceptionMapper;

import static org.fest.assertions.api.Assertions.assertThat;

public class TenacityBundleBuilderTest {
    private final TenacityPropertyKeyFactory propertyKeyFactory = new TenacityPropertyKeyFactory() {
        @Override
        public TenacityPropertyKey from(String value) {
            return null;
        }
    };

    private final Iterable<TenacityPropertyKey> propertyKeys = ImmutableList.of();

    @Test
    public void shouldBuild() {
        final TenacityBundle bundle = TenacityBundleBuilder
                .newBuilder()
                .propertyKeyFactory(propertyKeyFactory)
                .propertyKeys(propertyKeys)
                .build();

        assertThat(bundle).isEqualTo(new TenacityBundle(propertyKeyFactory, propertyKeys));
    }

    @Test(expected = IllegalArgumentException.class)
      public void shouldFailWithoutKeyFactory() {
        TenacityBundleBuilder
                .newBuilder()
                .propertyKeys(propertyKeys)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithoutKeys() {
        TenacityBundleBuilder
                .newBuilder()
                .propertyKeyFactory(propertyKeyFactory)
                .build();
    }

    @Test
    public void shouldUseExceptionMapper() {
        final TenacityBundle bundle = TenacityBundleBuilder
                .newBuilder()
                .propertyKeyFactory(propertyKeyFactory)
                .propertyKeys(propertyKeys)
                .addExceptionMapper(new TenacityExceptionMapper(429))
                .build();

        assertThat(bundle).isEqualTo(new TenacityBundle(propertyKeyFactory, propertyKeys,
                ImmutableList.<ExceptionMapper<? extends Throwable>>of(new TenacityExceptionMapper(429)), Optional.<HystrixCommandExecutionHook>absent()));
    }

    @Test
    public void useAllExceptionMappers() {
        final TenacityBundle bundle = TenacityBundleBuilder
                .newBuilder()
                .propertyKeyFactory(propertyKeyFactory)
                .propertyKeys(propertyKeys)
                .mapAllHystrixRuntimeExceptionsTo(429)
                .build();

        assertThat(bundle).isEqualTo(new TenacityBundle(propertyKeyFactory, propertyKeys,
                ImmutableList.<ExceptionMapper<? extends Throwable>>of(
                        new TenacityExceptionMapper(429),
                        new TenacityContainerExceptionMapper(429)),
                Optional.<HystrixCommandExecutionHook>absent()));
    }

    @Test
    public void withExecutionMappers() throws Exception {
        final HystrixCommandExecutionHook hook = new ExceptionLoggingCommandHook();
        final TenacityBundle bundle = TenacityBundleBuilder
                .newBuilder()
                .propertyKeyFactory(propertyKeyFactory)
                .propertyKeys(propertyKeys)
                .commandExecutionHook(hook)
                .build();

        assertThat(bundle).isEqualTo(new TenacityBundle(propertyKeyFactory, propertyKeys,
                ImmutableList.<ExceptionMapper<? extends Throwable>>of(), Optional.of(hook)));
    }
}
