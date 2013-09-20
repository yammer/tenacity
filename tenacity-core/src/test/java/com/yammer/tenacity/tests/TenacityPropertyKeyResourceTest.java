package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.GenericType;
import com.yammer.dropwizard.testing.ResourceTest;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;
import org.junit.Test;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;

public class TenacityPropertyKeyResourceTest extends ResourceTest {

    public static final String PROPERTY_KEY_URI = "/tenacity/propertykeys";
    private ImmutableList<TenacityPropertyKey> keys;

    @Override
    protected void setUpResources() throws Exception {
        keys = ImmutableList.<TenacityPropertyKey>of(DependencyKey.EXAMPLE, DependencyKey.SLEEP);
        addResource(new TenacityPropertyKeysResource(keys));
    }

    @Test
    public void testGetKeys() throws Exception {
        final Iterable<? extends TenacityPropertyKey> iterable = client().resource(PROPERTY_KEY_URI).get(new GenericType<ArrayList<DependencyKey>>() { });
        for (TenacityPropertyKey key : keys) {
            assertThat(iterable).contains(key);
        }
    }
}
