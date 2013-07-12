package com.yammer.tenacity.dashboard.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("/tenacity")
public class IndexResource {
    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public Response render() throws Exception {
        return Response.ok(Files.toString(new File(Resources.getResource("index.html").toURI()), Charsets.UTF_8)).build();
    }
}
