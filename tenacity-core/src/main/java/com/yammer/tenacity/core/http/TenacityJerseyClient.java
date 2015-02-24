package com.yammer.tenacity.core.http;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import java.net.URI;
import java.util.Objects;

public class TenacityJerseyClient extends Client {
    protected final TenacityWebResourceFactory webResourceFactory;
    protected final Client client;

    public TenacityJerseyClient(TenacityWebResourceFactory webResourceFactory, Client client) {
        this.webResourceFactory = webResourceFactory;
        this.client = client;
    }

    @Override
    public WebResource resource(URI u) {
        return webResourceFactory.webResource(client, u);
    }

    @Override
    public AsyncWebResource asyncResource(URI u) {
        return webResourceFactory.asyncWebResource(client, u);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webResourceFactory, client);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TenacityJerseyClient other = (TenacityJerseyClient) obj;
        return Objects.equals(this.webResourceFactory, other.webResourceFactory)
                && Objects.equals(this.client, other.client);
    }
}