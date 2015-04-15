package com.yammer.tenacity.core.http;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.util.Duration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

public class TenacityJerseyClient implements Client {
    protected final Client delegate;
    protected final TenacityPropertyKey tenacityPropertyKey;
    protected final Duration timeoutPadding;

    public TenacityJerseyClient(Client client, TenacityPropertyKey tenacityPropertyKey, Duration timeoutPadding) {
        this.delegate = client;
        this.tenacityPropertyKey = tenacityPropertyKey;
        this.timeoutPadding = timeoutPadding;
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public TenacityWebTarget target(String uri) {
        return new TenacityWebTarget(
                delegate.target(uri),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public TenacityWebTarget target(URI uri) {
        return new TenacityWebTarget(
                delegate.target(uri),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget target(UriBuilder uriBuilder) {
        return new TenacityWebTarget(
                delegate.target(uriBuilder),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget target(Link link) {
        return new TenacityWebTarget(
                delegate.target(link),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public Invocation.Builder invocation(Link link) {
        WebTarget t = new TenacityWebTarget(
                delegate.target(link),
                tenacityPropertyKey,
                timeoutPadding
        );
        final String acceptType = link.getType();
        return (acceptType != null) ? t.request(acceptType) : t.request();
    }

    @Override
    public SSLContext getSslContext() {
        return delegate.getSslContext();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return delegate.getHostnameVerifier();
    }

    @Override
    public Configuration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public Client property(String name, Object value) {
        delegate.property(name, value);
        return this;
    }

    @Override
    public Client register(Class<?> componentClass) {
        delegate.register(componentClass);
        return this;
    }

    @Override
    public Client register(Class<?> componentClass, int priority) {
        delegate.register(componentClass, priority);
        return this;
    }

    @Override
    public Client register(Class<?> componentClass, Class<?>... contracts) {
        delegate.register(componentClass, contracts);
        return this;
    }

    @Override
    public Client register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        delegate.register(componentClass, contracts);
        return this;
    }

    @Override
    public Client register(Object component) {
        delegate.register(component);
        return this;
    }

    @Override
    public Client register(Object component, int priority) {
        delegate.register(component, priority);
        return this;
    }

    @Override
    public Client register(Object component, Class<?>... contracts) {
        delegate.register(component, contracts);
        return this;
    }

    @Override
    public Client register(Object component, Map<Class<?>, Integer> contracts) {
        delegate.register(component, contracts);
        return this;
    }
}