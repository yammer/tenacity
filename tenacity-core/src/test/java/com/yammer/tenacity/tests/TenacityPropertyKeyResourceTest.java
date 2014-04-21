package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.GenericType;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;
import org.junit.Rule;
import org.junit.Test;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.ArrayList;
import java.util.List;
import static org.fest.assertions.api.Assertions.assertThat;

public class TenacityPropertyKeyResourceTest {

    public static final String PROPERTY_KEY_URI = "/tenacity/propertykeys";
    private final ImmutableList<TenacityPropertyKey> keys = ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXAMPLE, DependencyKey.SLEEP);

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TenacityPropertyKeysResource(keys)).build();

    @Test
    public void testGetKeys() throws Exception {
        final Iterable<? extends TenacityPropertyKey> iterable = resources.client().resource(PROPERTY_KEY_URI).get(new GenericType<ArrayList<DependencyKey>>() { });
        final List<TenacityPropertyKey> properties = Lists.newArrayList(iterable);
        for (TenacityPropertyKey key : properties) {
            assertThat(properties).contains(key);
        }
    }
}
