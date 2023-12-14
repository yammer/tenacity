package com.yammer.tenacity.client.tests;

import com.google.common.io.Resources;
import com.yammer.tenacity.client.TenacityClientBuilder;
import io.dropwizard.core.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import java.net.URI;

public class TenacityResourcesServletTest extends TenacityResources {
    @ClassRule
    public static DropwizardAppRule<Configuration> APP_RULE = new DropwizardAppRule<>(TenacityServletAdminService.class,
            Resources.getResource("tenacityPropertyKeyServletService.yml").getPath());

    @BeforeClass
    public static void initialization() {
        CLIENT = new TenacityClientBuilder(APP_RULE.getEnvironment(), ServletKeys.KEY_ONE)
                .build();
        URI_ROOT = URI.create("http://127.0.0.1:" + APP_RULE.getAdminPort());
        JERSEY_CLIENT = new JerseyClientBuilder(APP_RULE.getEnvironment())
                .build("test");
    }
}
