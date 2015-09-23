package com.yammer.tenacity.core.resources;

import com.codahale.metrics.annotation.Timed;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyStore;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;

@Path(TenacityConfigurationResource.PATH)
public class TenacityConfigurationResource {
    public static final String PATH = "/tenacity/configuration";
    private final TenacityPropertyKeyFactory factory;

    public TenacityConfigurationResource(TenacityPropertyKeyFactory factory) {
        this.factory = checkNotNull(factory);
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON) @Path("{key}")
    public Response get(@PathParam("key") String key) {
        try {
            return Response.ok(TenacityPropertyStore.getTenacityConfiguration(factory.from(key))).build();
        } catch (IllegalArgumentException err) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
