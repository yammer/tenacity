package com.yammer.tenacity.tests;

import com.yammer.dropwizard.testing.ResourceTest;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TenacityConfigurationResourceTest extends ResourceTest {

    public static final String TENACITY_CONFIGURATION_URI = "/tenacity/v1/configuration";
    private TenacityPropertyKeyFactory mock;

    @Override
    protected void setUpResources() throws Exception {
        mock = mock(TenacityPropertyKeyFactory.class);
        addResource(new TenacityConfigurationResource(mock));
    }

    @Test
    public void testGet() throws Exception {
        when(mock.from(anyString())).thenReturn(DependencyKey.EXAMPLE);
        final TenacityConfiguration tenacityConfiguration = client().resource(TENACITY_CONFIGURATION_URI).path(DependencyKey.EXAMPLE.toString()).get(TenacityConfiguration.class);
        assertThat(tenacityConfiguration).isNotNull();
    }
}
