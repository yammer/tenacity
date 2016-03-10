package com.sun.jersey.api.client;

import com.google.common.primitives.Ints;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.util.Duration;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Objects;

public class TenacityWebResource extends WebResource {
    protected final TenacityPropertyKey tenacityPropertyKey;
    protected final Duration timeoutPadding;

    public TenacityWebResource(ClientHandler c,
                               CopyOnWriteHashMap<String, Object> properties,
                               URI u,
                               TenacityPropertyKey tenacityPropertyKey,
                               Duration timeoutPadding) {
        super(c, properties, u);
        this.tenacityPropertyKey = tenacityPropertyKey;
        this.timeoutPadding = timeoutPadding;
    }

    @Override
    public <T> T get(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.get(c);
    }

    @Override
    public <T> T get(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.get(gt);
    }

    @Override
    public ClientResponse head() throws ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.head();
    }

    @Override
    public void put() throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        super.put();
    }

    @Override
    public void put(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        super.put(requestEntity);
    }

    @Override
    public <T> T put(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.put(c);
    }

    @Override
    public <T> T put(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.put(gt);
    }

    @Override
    public <T> T put(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.put(c, requestEntity);
    }

    @Override
    public <T> T put(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.put(gt, requestEntity);
    }

    @Override
    public void post() throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        super.post();
    }

    @Override
    public void post(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        super.post(requestEntity);
    }

    @Override
    public <T> T post(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.post(c);
    }

    @Override
    public <T> T post(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.post(gt);
    }

    @Override
    public <T> T post(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.post(c, requestEntity);
    }

    @Override
    public <T> T post(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.post(gt, requestEntity);
    }

    @Override
    public void delete() throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        super.delete();
    }

    @Override
    public void delete(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        super.delete(requestEntity);
    }

    @Override
    public <T> T delete(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.delete(c);
    }

    @Override
    public <T> T delete(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.delete(gt);
    }

    @Override
    public <T> T delete(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.delete(c, requestEntity);
    }

    @Override
    public <T> T delete(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.delete(gt, requestEntity);
    }

    @Override
    public void method(String method) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        super.method(method);
    }

    @Override
    public void method(String method, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        super.method(method, requestEntity);
    }

    @Override
    public <T> T method(String method, Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.method(method, c);
    }

    @Override
    public <T> T method(String method, GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.method(method, gt);
    }

    @Override
    public <T> T method(String method, Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.method(method, c, requestEntity);
    }

    @Override
    public <T> T method(String method, GenericType<T> gt, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.method(method, gt, requestEntity);
    }

    @Override
    public <T> T options(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.options(c);
    }

    @Override
    public <T> T options(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        setReadTimeoutWithTenacity();
        return super.options(gt);
    }

    @Override
    public Builder getRequestBuilder() {
        setReadTimeoutWithTenacity();
        return super.getRequestBuilder();
    }

    @Override
    public UriBuilder getUriBuilder() {
        setReadTimeoutWithTenacity();
        return super.getUriBuilder();
    }

    protected void setReadTimeoutWithTenacity() {
        setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, Ints.checkedCast(TenacityCommand
                .getCommandProperties(tenacityPropertyKey)
                .executionTimeoutInMilliseconds()
                .get() + timeoutPadding.toMilliseconds()));
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
        final TenacityWebResource other = (TenacityWebResource) obj;
        return Objects.equals(this.tenacityPropertyKey, other.tenacityPropertyKey)
                && Objects.equals(this.timeoutPadding, other.timeoutPadding);
    }
}