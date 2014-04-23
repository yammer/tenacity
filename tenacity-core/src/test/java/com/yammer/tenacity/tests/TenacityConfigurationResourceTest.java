package com.yammer.tenacity.tests;

import io.dropwizard.testing.junit.ResourceTestRule;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

public class TenacityConfigurationResourceTest {

    public static final String TENACITY_CONFIGURATION_URI = "/tenacity/configuration";
    private TenacityPropertyKeyFactory mock = mock(TenacityPropertyKeyFactory.class);

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TenacityConfigurationResource(mock)).build();
    
    @Before
    public void setUp() {
        reset(mock);
    }

    @Test
    public void testGet() throws Exception {
        when(mock.from(anyString())).thenReturn(DependencyKey.EXAMPLE);
        final TenacityConfiguration tenacityConfiguration = resources.client().resource(TENACITY_CONFIGURATION_URI).path(DependencyKey.EXAMPLE.toString()).get(TenacityConfiguration.class);
        assertThat(tenacityConfiguration).isNotNull();
    }
}
