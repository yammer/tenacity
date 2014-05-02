package com.yammer.tenacity.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.net.URI;

import static com.codahale.metrics.MetricRegistry.name;

public class TenacityClient {
    public static final String TENACITY_PROPERTYKEYS_PATH = "/tenacity/propertykeys";
    public static final String TENACITY_CONFIGURATION_PATH = "/tenacity/configuration";
    public static final String TENACITY_CIRCUITBREAKERS_PATH = "/tenacity/circuitbreakers";
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityClient.class);
    protected final Client client;
    protected final Timer fetchPropertyKeys;
    protected final Timer fetchConfiguration;
    protected final Timer fetchCircuitBreakers;

    public TenacityClient(MetricRegistry metricRegistry,
                          Client client) {
        this.client = client;
        this.fetchPropertyKeys = metricRegistry.timer(name(TenacityClient.class, "fetch-property-keys"));
        this.fetchConfiguration = metricRegistry.timer(name(TenacityClient.class, "fetch-configuration"));
        this.fetchCircuitBreakers = metricRegistry.timer(name(TenacityClient.class, "fetch-circuit-breakers"));
    }

    public Optional<ImmutableList<String>> getTenacityPropertyKeys(URI root) {
        try (Timer.Context timerContext = fetchPropertyKeys.time()) {
            return Optional.of(ImmutableList.copyOf(client.resource(root)
                    .path(TENACITY_PROPERTYKEYS_PATH)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(String[].class)));
        } catch (Exception err) {
            LOGGER.warn("Unable to retrieve property keys for {}", root, err);
        }
        return Optional.absent();
    }

    public Optional<TenacityConfiguration> getTenacityConfiguration(URI root, TenacityPropertyKey key) {
        try (Timer.Context timerContext = fetchConfiguration.time()) {
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
    }

    public Optional<ImmutableList<CircuitBreaker>> getCircuitBreakers(URI root) {
        try (Timer.Context timerContext = fetchCircuitBreakers.time()) {
            return Optional.of(ImmutableList.copyOf(client
                    .resource(root)
                    .path(TENACITY_CIRCUITBREAKERS_PATH)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(CircuitBreaker[].class)));
        } catch (Exception err) {
            LOGGER.warn("Unable to retrieve tenacity configuration for {} and key {}", root, err);
        }
        return Optional.absent();
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