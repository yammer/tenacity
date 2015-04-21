package com.yammer.tenacity.core.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.core.CircuitBreakers;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.NoSuchElementException;

@Path(TenacityCircuitBreakersResource.PATH)
public class TenacityCircuitBreakersResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityCircuitBreakersResource.class);
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
        return CircuitBreakers.all(keys);
    }

    @GET
    @Timed
    @Path("{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCircuitBreaker(@PathParam("key") String key ) {
        try {
            final TenacityPropertyKey foundKey = Iterables.find(keys, Predicates.equalTo(keyFactory.from(key)));
            final Optional<CircuitBreaker> circuitBreaker = CircuitBreaker.usingHystrix(foundKey);
            if (circuitBreaker.isPresent()) {
                return Response.ok(circuitBreaker.get()).build();
            }
        } catch (NoSuchElementException err) {
            LOGGER.warn("Could not find TenacityPropertyKey {}", key, err);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Timed
    @Path("{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyCircuitBreaker(@PathParam("key") String key,
                                         String body) {
        try {
            final TenacityPropertyKey foundKey = Iterables.find(keys, Predicates.equalTo(keyFactory.from(key)));
            final CircuitBreaker.State state = CircuitBreaker.State.valueOf(body.toUpperCase());
            switch (state) {
                case FORCED_CLOSED:
                    TenacityPropertyRegister.registerCircuitForceClosed(foundKey);
                    break;
                case FORCED_OPEN:
                    TenacityPropertyRegister.registerCircuitForceOpen(foundKey);
                    break;
                case FORCED_RESET:
                    TenacityPropertyRegister.registerCircuitForceReset(foundKey);
                    break;
                default:
                    throw new IllegalArgumentException("You cannot modify a circuit breaker with the state: " + state);
            }
            final Optional<CircuitBreaker> circuitBreaker = CircuitBreaker.usingHystrix(foundKey);
            if (circuitBreaker.isPresent()) {
                return Response.ok(circuitBreaker.get()).build();
            }
        } catch (NoSuchElementException err) {
            LOGGER.warn("Could not find TenacityPropertyKey {}", key, err);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}