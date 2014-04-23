package com.yammer.tenacity.client;

import com.sun.jersey.api.client.Client;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;

public class TenacityClientFactory {
    private final JerseyClientConfiguration jerseyConfiguration;

    public TenacityClientFactory(JerseyClientConfiguration configuration) {
        this.jerseyConfiguration = configuration;
    }

    public TenacityClient build(Environment environment) {
        final Client client = new JerseyClientBuilder(environment).using(jerseyConfiguration).build("tenacity");
        return new TenacityClient(client);
    }
}