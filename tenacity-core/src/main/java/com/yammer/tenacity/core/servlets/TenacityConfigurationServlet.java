package com.yammer.tenacity.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.tenacity.core.resources.TenacityConfigurationResource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TenacityConfigurationServlet extends TenacityServlet {
    private static final long serialVersionUID = 0;
    private transient final TenacityConfigurationResource configurationResource;

    public TenacityConfigurationServlet(ObjectMapper objectMapper,
                                        TenacityConfigurationResource configurationResource) {
        super(objectMapper);
        this.configurationResource = configurationResource;
    }

    @Override
    protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String key = req.getPathInfo().replaceAll("/", "");
        writeResponse(resp, configurationResource.get(key));
    }
}
