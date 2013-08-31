package com.yammer.tenacity.tests;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.tenacity.core.strategies.ManagedConcurrencyStrategy;
import com.yammer.tenacity.testing.TenacityTest;

import static org.mockito.Mockito.mock;

abstract class AbstractTenacityTest extends TenacityTest {
    static {
        final Environment environment = new Environment(
                "managed-concurrency-strategy-test",
                mock(Configuration.class),
                new ObjectMapperFactory(),
                new Validator());
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new ManagedConcurrencyStrategy(environment));
    }
}