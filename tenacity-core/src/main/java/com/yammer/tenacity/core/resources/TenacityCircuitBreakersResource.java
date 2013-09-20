package com.yammer.tenacity.core.resources;

import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.yammer.metrics.annotation.Timed;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/tenacity/circuitbreakers")
public class TenacityCircuitBreakersResource {
    private final Iterable<TenacityPropertyKey> keys;

    public TenacityCircuitBreakersResource(Iterable<TenacityPropertyKey> keys) {
        this.keys = checkNotNull(keys);
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<CircuitBreaker> circuitBreakers() {
        final ImmutableList.Builder<CircuitBreaker> circuitBreakerBuilder = ImmutableList.builder();
        for (TenacityPropertyKey key : keys) {
            final HystrixCircuitBreaker circuitBreaker = TenacityCommand.getCircuitBreaker(key);
            if (circuitBreaker != null) {
                circuitBreakerBuilder.add(new CircuitBreaker(key, circuitBreaker.isOpen()));
            }
        }
        return circuitBreakerBuilder.build();
    }
}
