package com.yammer.tenacity.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.yammer.tenacity.core.resources.TenacityCircuitBreakersResource;
import org.eclipse.jetty.servlets.GzipFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class TenacityCircuitBreakersServlet extends TenacityServlet {
    private static final long serialVersionUID = 0;
    private transient final TenacityCircuitBreakersResource circuitBreakersResource;
    private final Pattern byName = Pattern.compile("^/[\\d\\w\\s]*$");

    public TenacityCircuitBreakersServlet(ObjectMapper objectMapper,
                                          TenacityCircuitBreakersResource circuitBreakersResource) {
        super(objectMapper);
        this.circuitBreakersResource = circuitBreakersResource;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getPathInfo();
        if (path == null || path.isEmpty() || path.equals("/")) {
            writeResponse(resp, circuitBreakersResource.circuitBreakers());
        } else if (byName.matcher(path).matches()) {
            final String key = path.trim().substring(1);
            writeResponse(resp, circuitBreakersResource.getCircuitBreaker(key));
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getPathInfo();
        if (path != null && byName.matcher(path).matches()) {
            final String key = path.trim().substring(1);
            InputStream inputStream = req.getInputStream();
            if (GzipFilter.GZIP.equalsIgnoreCase(req.getHeader(HttpHeaders.CONTENT_ENCODING))) {
                inputStream = new GZIPInputStream(inputStream);
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                final String body = CharStreams.toString(reader);
                writeResponse(resp, circuitBreakersResource.modifyCircuitBreaker(key, body));
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
