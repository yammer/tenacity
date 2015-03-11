package com.sun.jersey.api.client;

import com.google.common.primitives.Ints;
import com.sun.jersey.api.client.async.ITypeListener;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.util.Duration;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Future;

public class TenacityAsyncWebResource extends AsyncWebResource {
    protected final TenacityPropertyKey tenacityPropertyKey;
    protected final Duration timeoutPadding;

    public TenacityAsyncWebResource(Client c,
                                    CopyOnWriteHashMap<String, Object> properties,
                                    URI u,
                                    TenacityPropertyKey tenacityPropertyKey,
                                    Duration timeoutPadding) {
        super(c, properties, u);
        this.tenacityPropertyKey = tenacityPropertyKey;
        this.timeoutPadding = timeoutPadding;
    }

    public TenacityAsyncWebResource(AsyncWebResource that,
                                    UriBuilder ub,
                                    TenacityPropertyKey tenacityPropertyKey,
                                    Duration timeoutPadding) {
        super(that, ub);
        this.tenacityPropertyKey = tenacityPropertyKey;
        this.timeoutPadding = timeoutPadding;
    }

    @Override
    public Future<ClientResponse> head() {
        setReadTimeoutWithTenacity();
        return super.head();
    }

    @Override
    public Future<ClientResponse> head(ITypeListener<ClientResponse> l) {
        setReadTimeoutWithTenacity();
        return super.head(l);
    }

    @Override
    public <T> Future<T> options(Class<T> c) {
        setReadTimeoutWithTenacity();
        return super.options(c);
    }

    @Override
    public <T> Future<T> options(GenericType<T> gt) {
        setReadTimeoutWithTenacity();
        return super.options(gt);
    }

    @Override
    public <T> Future<T> options(ITypeListener<T> l) {
        setReadTimeoutWithTenacity();
        return super.options(l);
    }

    @Override
    public <T> Future<T> get(Class<T> c) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.get(c);
    }

    @Override
    public <T> Future<T> get(GenericType<T> gt) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.get(gt);
    }

    @Override
    public <T> Future<T> get(ITypeListener<T> l) {
        setReadTimeoutWithTenacity();
        return super.get(l);
    }

    @Override
    public Future<?> put() throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.put();
    }

    @Override
    public Future<?> put(Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.put(requestEntity);
    }

    @Override
    public <T> Future<T> put(Class<T> c) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.put(c);
    }

    @Override
    public <T> Future<T> put(GenericType<T> gt) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.put(gt);
    }

    @Override
    public <T> Future<T> put(ITypeListener<T> l) {
        setReadTimeoutWithTenacity();
        return super.put(l);
    }

    @Override
    public <T> Future<T> put(Class<T> c, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.put(c, requestEntity);
    }

    @Override
    public <T> Future<T> put(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.put(gt, requestEntity);
    }

    @Override
    public <T> Future<T> put(ITypeListener<T> l, Object requestEntity) {
        setReadTimeoutWithTenacity();
        return super.put(l, requestEntity);
    }

    @Override
    public Future<?> post() throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.post();
    }

    @Override
    public Future<?> post(Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.post(requestEntity);
    }

    @Override
    public <T> Future<T> post(Class<T> c) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.post(c);
    }

    @Override
    public <T> Future<T> post(GenericType<T> gt) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.post(gt);
    }

    @Override
    public <T> Future<T> post(ITypeListener<T> l) {
        setReadTimeoutWithTenacity();
        return super.post(l);
    }

    @Override
    public <T> Future<T> post(Class<T> c, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.post(c, requestEntity);
    }

    @Override
    public <T> Future<T> post(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.post(gt, requestEntity);
    }

    @Override
    public <T> Future<T> post(ITypeListener<T> l, Object requestEntity) {
        setReadTimeoutWithTenacity();
        return super.post(l, requestEntity);
    }

    @Override
    public Future<?> delete() throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.delete();
    }

    @Override
    public Future<?> delete(Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.delete(requestEntity);
    }

    @Override
    public <T> Future<T> delete(Class<T> c) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.delete(c);
    }

    @Override
    public <T> Future<T> delete(GenericType<T> gt) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.delete(gt);
    }

    @Override
    public <T> Future<T> delete(ITypeListener<T> l) {
        setReadTimeoutWithTenacity();
        return super.delete(l);
    }

    @Override
    public <T> Future<T> delete(Class<T> c, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.delete(c, requestEntity);
    }

    @Override
    public <T> Future<T> delete(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.delete(gt, requestEntity);
    }

    @Override
    public <T> Future<T> delete(ITypeListener<T> l, Object requestEntity) {
        setReadTimeoutWithTenacity();
        return super.delete(l, requestEntity);
    }

    @Override
    public Future<?> method(String method) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.method(method);
    }

    @Override
    public Future<?> method(String method, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.method(method, requestEntity);
    }

    @Override
    public <T> Future<T> method(String method, Class<T> c) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.method(method, c);
    }

    @Override
    public <T> Future<T> method(String method, GenericType<T> gt) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.method(method, gt);
    }

    @Override
    public <T> Future<T> method(String method, ITypeListener<T> l) {
        setReadTimeoutWithTenacity();
        return super.method(method, l);
    }

    @Override
    public <T> Future<T> method(String method, Class<T> c, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.method(method, c, requestEntity);
    }

    @Override
    public <T> Future<T> method(String method, GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        setReadTimeoutWithTenacity();
        return super.method(method, gt, requestEntity);
    }

    @Override
    public <T> Future<T> method(String method, ITypeListener<T> l, Object requestEntity) {
        setReadTimeoutWithTenacity();
        return super.method(method, l, requestEntity);
    }

    @Override
    public UriBuilder getUriBuilder() {
        setReadTimeoutWithTenacity();
        return super.getUriBuilder();
    }

    @Override
    public Builder getRequestBuilder() {
        setReadTimeoutWithTenacity();
        return super.getRequestBuilder();
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(tenacityPropertyKey, timeoutPadding);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final TenacityAsyncWebResource other = (TenacityAsyncWebResource) obj;
        return Objects.equals(this.tenacityPropertyKey, other.tenacityPropertyKey)
                && Objects.equals(this.timeoutPadding, other.timeoutPadding);
    }

    protected void setReadTimeoutWithTenacity() {
        setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, Ints.checkedCast(TenacityCommand
                .getCommandProperties(tenacityPropertyKey)
                .executionTimeoutInMilliseconds()
                .get() + timeoutPadding.toMilliseconds()));
    }
}
