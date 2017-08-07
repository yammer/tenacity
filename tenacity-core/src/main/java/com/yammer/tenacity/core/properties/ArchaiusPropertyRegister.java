package com.yammer.tenacity.core.properties;

import com.google.common.primitives.Ints;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import com.netflix.config.sources.URLConfigurationSource;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import io.dropwizard.util.Duration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ArchaiusPropertyRegister {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchaiusPropertyRegister.class);


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
    public void register(BreakerboxConfiguration breakerboxConfiguration) {
        if (breakerboxConfiguration.getUrls().isEmpty()) {
            return;
        }

        final TenacityPollingScheduler tenacityPollingScheduler = new TenacityPollingScheduler(
                Ints.checkedCast(breakerboxConfiguration.getInitialDelay().toMilliseconds()),
                Ints.checkedCast(breakerboxConfiguration.getDelay().toMilliseconds()),
                true);

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        if (breakerboxConfiguration.isWaitForInitialLoad()) {
            tenacityPollingScheduler.addPollListener((eventType, lastResult, exception) -> countDownLatch.countDown());
        }

        final DynamicConfiguration dynConfig = new DynamicConfiguration(
                new URLConfigurationSource(breakerboxConfiguration.getUrls().split(",")),
                tenacityPollingScheduler);

        ConfigurationManager.getConfigInstance();
        ConfigurationManager.loadPropertiesFromConfiguration(dynConfig);

        if (breakerboxConfiguration.isWaitForInitialLoad()) {
            final Duration duration = breakerboxConfiguration.getWaitForInitialLoad();
            try {
                final boolean success = countDownLatch.await(duration.getQuantity(), duration.getUnit());
                LOGGER.info("Breakerbox initial configuration load: {}", success ? "SUCCESS" : "FAILURE");
            } catch (Exception err) {
                LOGGER.warn("Failed waiting for Breakerbox initial load", err);
            }
        }
    }
}
