package com.yammer.tenacity.core.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.NoSuchElementException;

@Path(TenacityCircuitBreakersResource.PATH)
public class TenacityCircuitBreakersResource {
    public static final String PATH = "/tenacity/circuitbreakers";
    private final Iterable<TenacityPropertyKey> keys;
    private final TenacityPropertyKeyFactory keyFactory;

    public TenacityCircuitBreakersResource(Iterable<TenacityPropertyKey> keys,
                                           TenacityPropertyKeyFactory keyFactory) {
        this.keys = keys;
        this.keyFactory = keyFactory;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<CircuitBreaker> circuitBreakers() {
        final ImmutableList.Builder<CircuitBreaker> circuitBreakerBuilder = ImmutableList.builder();
        for (TenacityPropertyKey key : keys) {
            final HystrixCircuitBreaker circuitBreaker = TenacityCommand.getCircuitBreaker(key);
            if (circuitBreaker != null) {
                circuitBreakerBuilder.add(new CircuitBreaker(key, !circuitBreaker.allowRequest()));
            }
        }
        return circuitBreakerBuilder.build();
    }

    @GET
    @Timed
    @Path("{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCircuitBreakerStatus(@PathParam("key") String key ) {
        try {
            final TenacityPropertyKey foundKey = Iterables.find(keys, Predicates.equalTo(keyFactory.from(key)));
            final HystrixCircuitBreaker circuitBreaker = TenacityCommand.getCircuitBreaker(foundKey);
            return Response.ok(new CircuitBreaker(foundKey, !circuitBreaker.allowRequest())).build();
        } catch (NoSuchElementException err) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}