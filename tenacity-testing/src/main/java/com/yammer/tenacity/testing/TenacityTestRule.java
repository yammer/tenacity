package com.yammer.tenacity.testing;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.concurrent.TimeUnit;

public class TenacityTestRule implements TestRule {
    private void setup() {
        Hystrix.reset();
        final AbstractConfiguration configuration = ConfigurationManager.getConfigInstance();
        configuration.setProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", "100");
    }

    public void teardown() {
        Hystrix.reset(1, TimeUnit.SECONDS);
        ConfigurationManager.getConfigInstance().clear();
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    setup();
                    base.evaluate();
                } finally {
                    teardown();
                }
            }
        };
    }
}