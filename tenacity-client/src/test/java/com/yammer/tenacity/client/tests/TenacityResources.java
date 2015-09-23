package com.yammer.tenacity.client.tests;

import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.HystrixCommandProperties;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class TenacityResources {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    protected static TenacityClient CLIENT;
    protected static URI URI_ROOT;
    protected static Client JERSEY_CLIENT;

    private Response response;

    private static void executeAllKeys() {
        for (TenacityPropertyKey key : ServletKeys.values()) {
            new TenacityCommand<Object>(key) {
                @Override
                protected Object run() throws Exception {
                    return null;
                }
            }.execute();
        }
    }

    private static Response circuitBreakers() {
        return JERSEY_CLIENT
                .target(URI_ROOT)
                .path(TenacityCircuitBreakersResource.PATH)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
    }

    private static Response circuitBreakersRequest(TenacityPropertyKey key) {
        return JERSEY_CLIENT
                .target(URI_ROOT)
                .path(TenacityCircuitBreakersResource.PATH)
                .path(key.toString())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
    }

    private static Response tenacityConfiguration(TenacityPropertyKey key) {
        return JERSEY_CLIENT
                .target(URI_ROOT)
                .path(TenacityConfigurationResource.PATH)
                .path(key.toString())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
    }

    @After
    public void teardown() {
        if (response != null) {
            response.close();
        }
    }

    @Test
    public void propertyKeys() {
        final ImmutableList.Builder<String> keys = ImmutableList.builder();
        for (TenacityPropertyKey key : ServletKeys.values()) {
            keys.add(key.toString());
        }
        assertThat(CLIENT.getTenacityPropertyKeys(URI_ROOT)).contains(keys.build());
    }

    @Test
    public void configurationDoesNotExist() {
        final TenacityPropertyKey nonExistentKey = new TenacityPropertyKey() {
            @Override
            public String name() {
                return "none";
            }
        };
        assertThat(CLIENT.getTenacityConfiguration(URI_ROOT, nonExistentKey)).isAbsent();

        response = tenacityConfiguration(nonExistentKey);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void configurationExist() {
        final TenacityConfiguration tenacityConfiguration = new TenacityConfiguration();
        tenacityConfiguration.setExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        assertThat(CLIENT.getTenacityConfiguration(URI_ROOT, ServletKeys.KEY_ONE))
                .contains(tenacityConfiguration);

        response = tenacityConfiguration(ServletKeys.KEY_ONE);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void circuitBreakersAllShouldBeEmpty() {
        assertThat(CLIENT.getCircuitBreakers(URI_ROOT)).contains(ImmutableList.of());
    }

    @Test
    public void circuitBreakersAllShouldBeClosed() {
        executeAllKeys();

        final ImmutableList.Builder<CircuitBreaker> builder = ImmutableList.builder();
        for (TenacityPropertyKey key : ServletKeys.values()) {
            builder.add(CircuitBreaker.closed(key));
        }

        assertThat(CLIENT.getCircuitBreakers(URI_ROOT)).contains(builder.build());

        response = circuitBreakers();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void circuitBreakerFetchNonExistent() {
        assertThat(CLIENT.getCircuitBreaker(URI_ROOT, ServletKeys.KEY_ONE))
                .isAbsent();

        response = circuitBreakersRequest(ServletKeys.KEY_ONE);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void circuitBreakerFetchExistent() {
        executeAllKeys();

        assertThat(CLIENT.getCircuitBreaker(URI_ROOT, ServletKeys.KEY_ONE))
                .contains(CircuitBreaker.closed(ServletKeys.KEY_ONE));

        response = circuitBreakersRequest(ServletKeys.KEY_ONE);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
    
    @Test
    public void modifyCircuitBreaker() {
        assertThat(CLIENT.modifyCircuitBreaker(URI_ROOT, ServletKeys.KEY_THREE, CircuitBreaker.State.FORCED_OPEN))
                .isAbsent();

        assertThat(CLIENT.getCircuitBreakers(URI_ROOT))
                .contains(Collections.emptyList());

        assertFalse(new TenacityCommand<Boolean>(ServletKeys.KEY_THREE) {
            @Override
            protected Boolean run() throws Exception {
                return true;
            }

            @Override
            protected Boolean getFallback() {
                return false;
            }
        }.execute());

        assertThat(CLIENT.getCircuitBreakers(URI_ROOT))
                .contains(ImmutableList.of(CircuitBreaker.forcedOpen(ServletKeys.KEY_THREE)));
        assertThat(CLIENT.getCircuitBreaker(URI_ROOT, ServletKeys.KEY_THREE))
                .contains(CircuitBreaker.forcedOpen(ServletKeys.KEY_THREE));

        assertThat(CLIENT.modifyCircuitBreaker(URI_ROOT, ServletKeys.KEY_THREE, CircuitBreaker.State.FORCED_CLOSED))
                .contains(CircuitBreaker.forcedClosed(ServletKeys.KEY_THREE));

        assertTrue(new TenacityCommand<Boolean>(ServletKeys.KEY_THREE) {
            @Override
            protected Boolean run() throws Exception {
                return true;
            }

            @Override
            protected Boolean getFallback() {
                return false;
            }
        }.execute());

        assertThat(CLIENT.getCircuitBreakers(URI_ROOT))
                .contains(ImmutableList.of(CircuitBreaker.forcedClosed(ServletKeys.KEY_THREE)));
        assertThat(CLIENT.getCircuitBreaker(URI_ROOT, ServletKeys.KEY_THREE))
                .contains(CircuitBreaker.forcedClosed(ServletKeys.KEY_THREE));

        assertThat(CLIENT.modifyCircuitBreaker(URI_ROOT, ServletKeys.KEY_THREE, CircuitBreaker.State.FORCED_RESET))
                .contains(CircuitBreaker.closed(ServletKeys.KEY_THREE));
        assertThat(CLIENT.getCircuitBreakers(URI_ROOT))
                .contains(ImmutableList.of(CircuitBreaker.closed(ServletKeys.KEY_THREE)));
    }

}
