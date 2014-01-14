package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.core.bundle.TenacityBundle;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import org.junit.Test;

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
        assertThat(bundle).isEqualTo(new TenacityBundle(propertyKeyFactory, propertyKeys, new TenacityExceptionMapper(500)));
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
                .exceptionMapper(new TenacityExceptionMapper(429))
                .build();

        assertThat(bundle).isEqualTo(new TenacityBundle(propertyKeyFactory, propertyKeys, new TenacityExceptionMapper(429)));
    }
}
