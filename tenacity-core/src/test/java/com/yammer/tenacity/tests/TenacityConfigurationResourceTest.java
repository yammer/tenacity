package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TenacityConfigurationResourceTest {

    public static final String TENACITY_CONFIGURATION_URI = "/tenacity/configuration";
    private TenacityPropertyKeyFactory tenacityPropertyKeyFactoryMock = mock(TenacityPropertyKeyFactory.class);

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TenacityConfigurationResource(tenacityPropertyKeyFactoryMock)).build();

    @Before
    public void setUp() {
        reset(tenacityPropertyKeyFactoryMock);
    }

    @Test
    public void testGet() throws Exception {
        when(tenacityPropertyKeyFactoryMock.from(anyString())).thenReturn(DependencyKey.EXAMPLE);
        final TenacityConfiguration tenacityConfiguration = resources.client()
                .target(TENACITY_CONFIGURATION_URI)
                .path(DependencyKey.EXAMPLE.toString())
                .request()
                .get(TenacityConfiguration.class);

        assertThat(tenacityConfiguration).isNotNull();
    }
}