package com.yammer.tenacity.core.resources;

import com.google.common.collect.ImmutableList;
import com.yammer.metrics.annotation.Timed;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/tenacity/v1/propertykeys")
@Deprecated
public class DeprecatedTenacityPropertyKeysResource {
    private final Iterable<TenacityPropertyKey> keys;

    public DeprecatedTenacityPropertyKeysResource(TenacityPropertyKey[] keys) {
        this(ImmutableList.copyOf(keys));
    }

    public DeprecatedTenacityPropertyKeysResource(Iterable<TenacityPropertyKey> keys) {
        this.keys = checkNotNull(keys);
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    public Iterable<TenacityPropertyKey> getKeys() {
        return keys;
    }
}
