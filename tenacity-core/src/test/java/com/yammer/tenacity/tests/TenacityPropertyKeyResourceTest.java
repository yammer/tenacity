package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

public class TenacityPropertyKeyResourceTest {

    public static final String PROPERTY_KEY_URI = "/tenacity/propertykeys";
    private final ImmutableList<TenacityPropertyKey> keys = ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXAMPLE, DependencyKey.SLEEP);

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TenacityPropertyKeysResource(keys)).build();

    @Test
    public void testGetKeys() throws Exception {
        final Iterable<? extends TenacityPropertyKey> iterable =
                resources.client()
                        .target(PROPERTY_KEY_URI)
                        .request()
                        .get(new GenericType<ArrayList<DependencyKey>>() {
                        });
        final List<TenacityPropertyKey> properties = Lists.newArrayList(iterable);
        for (TenacityPropertyKey key : properties) {
            assertThat(properties, contains(key));
        }
    }
}
