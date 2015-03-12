package com.sun.jersey.api.client;

import com.google.common.primitives.Ints;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.util.Duration;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class TenacityWebTarget implements WebTarget {
    private final WebTarget delegate;
    protected final TenacityPropertyKey tenacityPropertyKey;
    protected final Duration timeoutPadding;


    public TenacityWebTarget(WebTarget delegate, TenacityPropertyKey tenacityPropertyKey, Duration timeoutPadding) {
        this.delegate = delegate;
        this.tenacityPropertyKey = tenacityPropertyKey;
        this.timeoutPadding = timeoutPadding;
    }


    protected void setTimeoutWithTenacity() {
        delegate.property(ClientProperties.READ_TIMEOUT, Ints.checkedCast(TenacityCommand
                        .getCommandProperties(tenacityPropertyKey)
                        .executionTimeoutInMilliseconds()
                        .get() + timeoutPadding.toMilliseconds()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate, tenacityPropertyKey, timeoutPadding);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TenacityWebTarget other = (TenacityWebTarget) obj;
        return Objects.equals(this.delegate, other.delegate)
                && Objects.equals(this.tenacityPropertyKey, other.tenacityPropertyKey)
                && Objects.equals(this.timeoutPadding, other.timeoutPadding);
    }

    @Override
    public URI getUri() {
        return delegate.getUri();
    }

    @Override
    public UriBuilder getUriBuilder() {
        return delegate.getUriBuilder();
    }

    @Override
    public WebTarget path(String path) {
        return new TenacityWebTarget(
                delegate.path(path),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget resolveTemplate(String name, Object value) {
        return new TenacityWebTarget(
                delegate.resolveTemplate(name, value),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        return new TenacityWebTarget(
                delegate.resolveTemplate(name, value, encodeSlashInPath),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget resolveTemplateFromEncoded(String name, Object value) {
        return new TenacityWebTarget(
                delegate.resolveTemplateFromEncoded(name, value),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> templateValues) {
        return new TenacityWebTarget(
                delegate.resolveTemplates(templateValues),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) {
        return new TenacityWebTarget(
                delegate.resolveTemplates(templateValues, encodeSlashInPath),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        return new TenacityWebTarget(
                delegate.resolveTemplatesFromEncoded(templateValues),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget matrixParam(String name, Object... values) {
        return new TenacityWebTarget(
                delegate.matrixParam(name, values),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget queryParam(String name, Object... values) {
        return new TenacityWebTarget(
                delegate.queryParam(name, values),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public Invocation.Builder request() {
        setTimeoutWithTenacity();
        return delegate.request();
    }

    @Override
    public Invocation.Builder request(String... acceptedResponseTypes) {
        setTimeoutWithTenacity();
        return delegate.request(acceptedResponseTypes);
    }

    @Override
    public Invocation.Builder request(MediaType... acceptedResponseTypes) {
        setTimeoutWithTenacity();
        return delegate.request(acceptedResponseTypes);
    }

    @Override
    public Configuration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public WebTarget property(String name, Object value) {
        return new TenacityWebTarget(
                delegate.property(name, value),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget register(Class<?> componentClass) {
        return new TenacityWebTarget(
                delegate.register(componentClass),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget register(Class<?> componentClass, int priority) {
        return new TenacityWebTarget(
                delegate.register(componentClass, priority),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget register(Class<?> componentClass, Class<?>... contracts) {
        return new TenacityWebTarget(
                delegate.register(componentClass, contracts),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        return new TenacityWebTarget(
                delegate.register(componentClass, contracts),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget register(Object component) {
        return new TenacityWebTarget(
                delegate.register(component),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget register(Object component, int priority) {
        return new TenacityWebTarget(
                delegate.register(component, priority),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget register(Object component, Class<?>... contracts) {
        return new TenacityWebTarget(
                delegate.register(component, contracts),
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    @Override
    public WebTarget register(Object component, Map<Class<?>, Integer> contracts) {
        return new TenacityWebTarget(
                delegate.register(component, contracts),
                tenacityPropertyKey,
                timeoutPadding
        );
    }
}
