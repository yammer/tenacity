package com.yammer.tenacity.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;

abstract class TenacityServlet extends HttpServlet {
    protected final ObjectMapper objectMapper;

    public TenacityServlet(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void setHeaders(HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON);
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
    }

    void writeResponse(HttpServletResponse resp,
                       Response response) throws IOException {
        setHeaders(resp);

        try (final OutputStream output = resp.getOutputStream()) {
            switch (response.getStatus()) {
                case HttpServletResponse.SC_OK:
                    resp.setStatus(HttpServletResponse.SC_OK);
                    objectMapper.writer().writeValue(output, response.getEntity());
                    break;
                case HttpServletResponse.SC_NOT_FOUND:
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    break;
            }
        }
    }

    void writeResponse(HttpServletResponse resp,
                       Iterable<?> iterable) throws IOException {
        setHeaders(resp);

        try (final OutputStream output = resp.getOutputStream()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writer().writeValue(output, iterable);
        }
    }
}
