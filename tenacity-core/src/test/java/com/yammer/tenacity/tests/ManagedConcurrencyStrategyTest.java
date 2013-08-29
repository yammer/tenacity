package com.yammer.tenacity.tests;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.strategies.ManagedConcurrencyStrategy;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ManagedConcurrencyStrategyTest {
    static ManagedConcurrencyStrategy managedConcurrencyStrategy;

    @BeforeClass
    public static void initialization() throws Exception {
        final Environment environment = new Environment(
                "managed-concurrency-strategy-test",
                ConfigurationFactory.forClass(Configuration.class, new Validator()).build(),
                new ObjectMapperFactory(),
                new Validator());
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new ManagedConcurrencyStrategy(environment));
    }

    private static enum Key implements TenacityPropertyKey {
        KEY
    }

    private static class SimpleCommand extends TenacityCommand<String> {
        private SimpleCommand() {
            super(Key.KEY);
        }

        @Override
        protected String run() throws Exception {
            return Key.KEY.toString();
        }

        @Override
        protected String getFallback() {
            return "failure";
        }
    }

    @Test
    public void simpleExecution() throws Exception {
        assertThat(new SimpleCommand().execute()).isEqualTo(Key.KEY.toString());
        assertThat(new SimpleCommand().queue().get()).isEqualTo(Key.KEY.toString());
    }
}
