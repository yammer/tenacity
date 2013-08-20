package com.yammer.tenacity.core.properties;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import com.netflix.config.sources.URLConfigurationSource;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchaiusPropertyRegister {
    private static class TenacityPollingScheduler extends FixedDelayPollingScheduler {
        private static final Logger LOGGER = LoggerFactory.getLogger(TenacityPollingScheduler.class);

        public TenacityPollingScheduler(int initialDelayMillis, int delayMillis, boolean ignoreDeletesFromSource) {
            super(initialDelayMillis, delayMillis, ignoreDeletesFromSource);
        }

        @Override
        protected synchronized void initialLoad(PolledConfigurationSource source, Configuration config) {
            try {
                super.initialLoad(source, config);
            } catch (Exception err) {
                LOGGER.warn("Initial dynamic configuration load failed", err);
            }
        }
    }
    public static void register(BreakerboxConfiguration breakerboxConfiguration) {
        ConfigurationManager.install(
                new DynamicConfiguration(
                    new URLConfigurationSource(breakerboxConfiguration.getUrls()),
                    new TenacityPollingScheduler(
                            breakerboxConfiguration.getInitialDelay(),
                            breakerboxConfiguration.getDelay(),
                            false)));
    }
}