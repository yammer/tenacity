package com.yammer.tenacity.client;

import com.sun.jersey.api.client.Client;
import com.yammer.tenacity.core.http.TenacityJerseyClientBuilder;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;

public class TenacityClientBuilder {
    protected JerseyClientConfiguration jerseyConfiguration = new JerseyClientConfiguration();
    protected final Environment environment;
    protected final TenacityPropertyKey tenacityPropertyKey;

    public TenacityClientBuilder(Environment environment,
                                 TenacityPropertyKey tenacityPropertyKey) {
        this.environment = environment;
        this.tenacityPropertyKey = tenacityPropertyKey;
    }

    public TenacityClientBuilder using(JerseyClientConfiguration jerseyConfiguration) {
        this.jerseyConfiguration = jerseyConfiguration;
        return this;
    }

    public TenacityClient build() {
        final Client client = new JerseyClientBuilder(environment)
                .using(jerseyConfiguration)
                .build("tenacity-" + tenacityPropertyKey);
        return new TenacityClient(environment.metrics(), TenacityJerseyClientBuilder
                .builder(tenacityPropertyKey)
                .build(client));
    }
}