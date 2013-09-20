package com.yammer.tenacity.core.resources;

import com.yammer.metrics.annotation.Timed;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyStore;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/tenacity/v1/configuration/{key}")
@Deprecated
public class DeprecatedTenacityConfigurationResource {
    private final TenacityPropertyKeyFactory factory;

    public DeprecatedTenacityConfigurationResource(TenacityPropertyKeyFactory factory) {
        this.factory = checkNotNull(factory);
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    public TenacityConfiguration get(@PathParam("key") String key) {
        return TenacityPropertyStore.getTenacityConfiguration(factory.from(key));
    }
}
