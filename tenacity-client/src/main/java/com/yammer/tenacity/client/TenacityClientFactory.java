package com.yammer.tenacity.client;

import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.client.config.TenacityClientConfiguration;

public class TenacityClientFactory {
    private final JerseyClientBuilder jerseyClientBuilder;
    private final TenacityClientConfiguration clientConfiguration;

    public TenacityClientFactory(JerseyClientConfiguration configuration,
                                 TenacityClientConfiguration clientConfiguration) {
        this.jerseyClientBuilder = new JerseyClientBuilder().using(configuration);
        this.clientConfiguration = clientConfiguration;
    }

    public TenacityClient build(Environment environment) {
        return new TenacityClient(
                jerseyClientBuilder.using(environment).build(),
                clientConfiguration.getUri());
    }
}