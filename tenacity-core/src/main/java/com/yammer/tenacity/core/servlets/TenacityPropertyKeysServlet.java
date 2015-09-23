package com.yammer.tenacity.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.tenacity.core.resources.TenacityPropertyKeysResource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TenacityPropertyKeysServlet extends TenacityServlet {
    private static final long serialVersionUID = 0;
    private transient final TenacityPropertyKeysResource propertyKeysResource;

    public TenacityPropertyKeysServlet(ObjectMapper objectMapper,
                                       TenacityPropertyKeysResource propertyKeysResource) {
        super(objectMapper);
        this.propertyKeysResource = propertyKeysResource;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        writeResponse(resp, propertyKeysResource.getKeys());
    }
}
