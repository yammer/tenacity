package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableMap;
import com.netflix.config.*;
import com.netflix.config.sources.URLConfigurationSource;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.util.Duration;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ArchaiusPropertyRegisterTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    private BreakerboxConfiguration breakerboxConfiguration;

    @Before
    public void setup() {
        breakerboxConfiguration = new BreakerboxConfiguration();
        breakerboxConfiguration.setUrls("http://127.0.0.1");
        breakerboxConfiguration.setDelay(Duration.milliseconds(100));
    }

    @Test
    public void registerTenacityConfigurationFirst() {
        new TenacityPropertyRegister(ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(
            DependencyKey.EXAMPLE, new TenacityConfiguration()), breakerboxConfiguration).register();
        assertThat(ConfigurationManager.getConfigInstance()).isInstanceOf(AggregatedConfiguration.class);
        assertThat(DynamicPropertyFactory.getInstance()
                .getLongProperty(TenacityPropertyRegister.executionIsolationThreadTimeoutInMilliseconds(DependencyKey.EXAMPLE),
                        0).get())
                .isEqualTo(1000);

        final ConcurrentCompositeConfiguration overrideConfig = new ConcurrentCompositeConfiguration();
        overrideConfig.setProperty(TenacityPropertyRegister.executionIsolationThreadTimeoutInMilliseconds(DependencyKey.EXAMPLE), 2000);
        ConfigurationManager.loadPropertiesFromConfiguration(overrideConfig);

        assertThat(DynamicPropertyFactory.getInstance()
                .getLongProperty(TenacityPropertyRegister.executionIsolationThreadTimeoutInMilliseconds(DependencyKey.EXAMPLE),
                        1000).get())
                .isEqualTo(2000);

        assertThat(DynamicPropertyFactory.getInstance()
                .getLongProperty(TenacityPropertyRegister.threadpoolCoreSize(DependencyKey.EXAMPLE),
                        1000).get())
                .isEqualTo(10);
    }

    @Test
    public void registerTenacityConfigurationAfter() {
        final ConcurrentCompositeConfiguration overrideConfig = new ConcurrentCompositeConfiguration();
        overrideConfig.setProperty(TenacityPropertyRegister.executionIsolationThreadTimeoutInMilliseconds(DependencyKey.EXAMPLE), 2000);
        ConfigurationManager.getConfigInstance();
        ConfigurationManager.loadPropertiesFromConfiguration(overrideConfig);

        assertThat(DynamicPropertyFactory.getInstance()
                .getLongProperty(TenacityPropertyRegister.executionIsolationThreadTimeoutInMilliseconds(DependencyKey.EXAMPLE),
                        57).get())
                .isEqualTo(2000);

        assertThat(DynamicPropertyFactory.getInstance()
                .getLongProperty(TenacityPropertyRegister.threadpoolCoreSize(DependencyKey.EXAMPLE),
                        57).get())
                .isEqualTo(57);

        new TenacityPropertyRegister(ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(
                DependencyKey.EXAMPLE, new TenacityConfiguration()), breakerboxConfiguration).register();

        assertThat(DynamicPropertyFactory.getInstance()
                .getLongProperty(TenacityPropertyRegister.executionIsolationThreadTimeoutInMilliseconds(DependencyKey.EXAMPLE),
                        57).get())
                .isEqualTo(2000);
        /* NOT 1000, because 2000 was set previously. If you wanted 1000, we'd have to specifically do a
        *  set override otherwise it takes the previous value.
        */

        assertThat(DynamicPropertyFactory.getInstance()
                .getLongProperty(TenacityPropertyRegister.threadpoolCoreSize(DependencyKey.EXAMPLE),
                        57).get())
                .isEqualTo(10);
    }

    @Test
    public void registerTwoSources() throws Exception {
        new TenacityPropertyRegister(ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(
                DependencyKey.EXAMPLE, new TenacityConfiguration()), breakerboxConfiguration).register();
        assertThat(ConfigurationManager.getConfigInstance()).isInstanceOf(AggregatedConfiguration.class);

        final AbstractPollingScheduler mockPollingSchedulerOne = mock(AbstractPollingScheduler.class);
        final DynamicConfiguration dynConfig = new DynamicConfiguration(
                new URLConfigurationSource("http://localhost"),
                mockPollingSchedulerOne);

        ConfigurationManager.loadPropertiesFromConfiguration(dynConfig);

        final AbstractPollingScheduler mockPollingSchedulerTwo = mock(AbstractPollingScheduler.class);
        final DynamicConfiguration dynConfig2 = new DynamicConfiguration(
                new URLConfigurationSource("http://localhost2"),
                mockPollingSchedulerTwo);

        ConfigurationManager.loadPropertiesFromConfiguration(dynConfig2);

        verify(mockPollingSchedulerOne, atLeastOnce()).startPolling(any(PolledConfigurationSource.class), any(Configuration.class));
        verify(mockPollingSchedulerTwo, atLeastOnce()).startPolling(any(PolledConfigurationSource.class), any(Configuration.class));
    }
}
