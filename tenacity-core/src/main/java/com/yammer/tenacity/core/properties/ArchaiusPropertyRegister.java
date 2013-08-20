package com.yammer.tenacity.core.properties;

import com.netflix.config.ConfigurationManager;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import org.apache.commons.configuration.AbstractConfiguration;

public class ArchaiusPropertyRegister {
    public static void register(BreakerboxConfiguration breakerboxConfiguration) {
        final AbstractConfiguration configuration = ConfigurationManager.getConfigInstance();
        configuration.setProperty("archaius.configurationSource.additionalUrls", breakerboxConfiguration.getUrls());
        configuration.setProperty("archaius.fixedDelayPollingScheduler.initialDelayMills", breakerboxConfiguration.getInitialDelay());
        configuration.setProperty("archaius.fixedDelayPollingScheduler.delayMills", breakerboxConfiguration.getDelay());
    }
}