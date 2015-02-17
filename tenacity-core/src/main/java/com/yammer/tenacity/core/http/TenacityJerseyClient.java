package com.yammer.tenacity.core.http;

import com.google.common.primitives.Ints;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.util.Duration;

import java.net.URI;
import java.util.Objects;

public class TenacityJerseyClient extends Client {
    protected final TenacityPropertyKey tenacityPropertyKey;
    protected final Client client;
    protected final Duration timeoutPadding;

    public TenacityJerseyClient(TenacityPropertyKey tenacityPropertyKey,
                                Duration timeoutPadding,
                                Client client) {
        this.tenacityPropertyKey = tenacityPropertyKey;
        this.timeoutPadding = timeoutPadding;
        this.client = client;
    }

    protected void setReadTimeoutWithTenacity() {
        client.setReadTimeout(Ints.checkedCast(TenacityCommand
                .getCommandProperties(tenacityPropertyKey)
                .executionIsolationThreadTimeoutInMilliseconds()
                .get() + timeoutPadding.toMilliseconds()));
    }

    @Override
    public WebResource resource(URI u) {
        setReadTimeoutWithTenacity();
        return client.resource(u);
    }

    @Override
    public AsyncWebResource asyncResource(URI u) {
        setReadTimeoutWithTenacity();
        return client.asyncResource(u);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenacityPropertyKey, client, timeoutPadding);
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
        return Objects.equals(this.tenacityPropertyKey, other.tenacityPropertyKey)
                && Objects.equals(this.client, other.client)
                && Objects.equals(this.timeoutPadding, other.timeoutPadding);
    }
}