package com.yammer.tenacity.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;

import javax.ws.rs.core.MediaType;
import java.net.URI;

public class TenacityClient {
    private final Client client;
    public static final String TENACITY_PROPERTYKEYS_PATH = "/tenacity/propertykeys";

    public TenacityClient(Client client) {
        this.client = client;
    }

    public Optional<ImmutableList<String>> getTenacityPropertyKeys(URI root) {
        return Optional.of(ImmutableList.copyOf(client.resource(root)
                .path(TENACITY_PROPERTYKEYS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(String[].class)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenacityClient that = (TenacityClient) o;

        if (!client.equals(that.client)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return client.hashCode();
    }
}