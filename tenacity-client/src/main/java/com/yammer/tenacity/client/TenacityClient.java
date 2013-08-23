package com.yammer.tenacity.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.net.URI;

public class TenacityClient {
    private final Client client;
    public static final String TENACITY_PROPERTYKEYS_PATH = "/tenacity/propertykeys";
    public static final String TENACITY_CONFIGURATION_PATH = "/tenacity/configuration";
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityClient.class);
    private static final Timer TIMER_GET_CONFIGURATION = Metrics.newTimer(TenacityClient.class, "get-configuration");
    private static final Timer TIMER_GET_PROPERTYKEYS = Metrics.newTimer(TenacityClient.class, "get-propertykeys");

    public TenacityClient(Client client) {
        this.client = client;
    }

    public Optional<ImmutableList<String>> getTenacityPropertyKeys(URI root) {
        final TimerContext timerContext = TIMER_GET_PROPERTYKEYS.time();
        try {
            try {
                return Optional.of(ImmutableList.copyOf(client.resource(root)
                        .path(TENACITY_PROPERTYKEYS_PATH)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(String[].class)));
            } catch (Exception err) {
                LOGGER.warn("Unable to retrieve property keys for {}", root, err);
            }
            return Optional.absent();
        } finally {
            timerContext.stop();
        }
    }

    public Optional<TenacityConfiguration> getTenacityConfiguration(URI root, TenacityPropertyKey key) {
        final TimerContext timerContext = TIMER_GET_CONFIGURATION.time();
        try {
            try {
                return Optional.of(client
                        .resource(root)
                        .path(TENACITY_CONFIGURATION_PATH)
                        .path(key.toString())
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(TenacityConfiguration.class));
            } catch (Exception err) {
                LOGGER.warn("Unable to retrieve tenacity configuration for {} and key {}", root, key, err);
            }
            return Optional.absent();
        } finally {
            timerContext.stop();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenacityClient that = (TenacityClient) o;

        if (!client.equals(that.client)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return client.hashCode();
    }
}