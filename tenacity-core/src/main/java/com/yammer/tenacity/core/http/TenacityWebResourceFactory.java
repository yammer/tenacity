package com.yammer.tenacity.core.http;

import com.sun.jersey.api.client.*;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.util.Duration;

import java.net.URI;
import java.util.Objects;

public class TenacityWebResourceFactory {
    protected final TenacityPropertyKey tenacityPropertyKey;
    protected final Duration timeoutPadding;

    public TenacityWebResourceFactory(TenacityPropertyKey tenacityPropertyKey, Duration timeoutPadding) {
        this.tenacityPropertyKey = tenacityPropertyKey;
        this.timeoutPadding = timeoutPadding;
    }

    public TenacityPropertyKey getTenacityPropertyKey() {
        return tenacityPropertyKey;
    }

    public Duration getTimeoutPadding() {
        return timeoutPadding;
    }

    public WebResource webResource(Client client, URI uri) {
        return new TenacityWebResource(
                client,
                (CopyOnWriteHashMap<String, Object>)client.getProperties(),
                uri,
                tenacityPropertyKey,
                timeoutPadding);
    }

    public AsyncWebResource asyncWebResource(Client client, URI uri) {
        return new TenacityAsyncWebResource(
                client,
                (CopyOnWriteHashMap<String, Object>)client.getProperties(),
                uri,
                tenacityPropertyKey,
                timeoutPadding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenacityPropertyKey, timeoutPadding);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TenacityWebResourceFactory other = (TenacityWebResourceFactory) obj;
        return Objects.equals(this.tenacityPropertyKey, other.tenacityPropertyKey)
                && Objects.equals(this.timeoutPadding, other.timeoutPadding);
    }
}