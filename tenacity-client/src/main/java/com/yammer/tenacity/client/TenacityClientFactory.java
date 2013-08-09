package com.yammer.tenacity.client;

import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Environment;

public class TenacityClientFactory {
    private final JerseyClientBuilder jerseyClientBuilder;

    public TenacityClientFactory(JerseyClientConfiguration configuration) {
        this.jerseyClientBuilder = new JerseyClientBuilder().using(configuration);
    }

    public TenacityClient build(Environment environment) {
        return new TenacityClient(jerseyClientBuilder.using(environment).build());
    }
}