package com.yammer.tenacity.core.resources;

import com.google.common.collect.ImmutableList;
import com.yammer.metrics.annotation.Timed;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("tenacity/propertykeys")
public class TenacityPropertyKeysResource {
    private final Iterable<TenacityPropertyKey> keys;

    public TenacityPropertyKeysResource(TenacityPropertyKey[] keys) {
        this(ImmutableList.copyOf(keys));
    }

    public TenacityPropertyKeysResource(Iterable<TenacityPropertyKey> keys) {
        this.keys = checkNotNull(keys);
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    public Iterable<TenacityPropertyKey> keys() {
        return keys;
    }
}