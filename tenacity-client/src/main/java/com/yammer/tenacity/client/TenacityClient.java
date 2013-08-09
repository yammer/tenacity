package com.yammer.tenacity.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;

import javax.ws.rs.core.MediaType;
import java.net.URI;

public class TenacityClient {
    private final Client client;
    private final URI root;

    public TenacityClient(Client client, URI root) {
        this.client = client;
        this.root = root;
    }

    public Optional<ImmutableList<String>> getTenacityPropertyKeys() {
        return Optional.of(ImmutableList.copyOf(client.resource(root)
                .path("/tenacity/propertykeys")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(String[].class)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenacityClient that = (TenacityClient) o;

        if (!client.equals(that.client)) return false;
        if (!root.equals(that.root)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = client.hashCode();
        result = 31 * result + root.hashCode();
        return result;
    }
}
