package com.yammer.tenacity.core.http;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.util.Duration;

import javax.ws.rs.client.Client;

public class TenacityJerseyClientBuilder {
    protected final TenacityPropertyKey tenacityPropertyKey;
    //50ms by default, because with lower values we have seen issues with it racing with Tenacity/Hystrix's timeout
    protected Duration timeoutPadding = Duration.milliseconds(50);

    private TenacityJerseyClientBuilder(TenacityPropertyKey tenacityPropertyKey) {
        this.tenacityPropertyKey = tenacityPropertyKey;
    }

    public TenacityJerseyClientBuilder usingTimeoutPadding(Duration timeoutPadding) {
        this.timeoutPadding = timeoutPadding;
        return this;
    }

    public Client build(Client client) {
        return new TenacityJerseyClient(
                client,
                tenacityPropertyKey,
                timeoutPadding
        );
    }

    public static TenacityJerseyClientBuilder builder(TenacityPropertyKey key) {
        return new TenacityJerseyClientBuilder(key);
    }
}