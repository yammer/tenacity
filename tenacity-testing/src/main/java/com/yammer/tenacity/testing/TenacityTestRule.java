package com.yammer.tenacity.testing;

import com.google.common.base.Throwables;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TenacityTestRule implements TestRule {
    private void setup() {
        resetHystrixPlugins();
        ConfigurationManager
                .getConfigInstance()
                .setProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", "1");
    }

    public void teardown() {
        Hystrix.reset(1, TimeUnit.SECONDS);
        ConfigurationManager.getConfigInstance().clear();
        resetHystrixPlugins();
    }

    private static void resetHystrixPlugins() {
        new HystrixPlugins.UnitTest().reset();
        resetCommandExecutionHook();
    }

    /**
     * There is a bug in {@link com.netflix.hystrix.strategy.HystrixPlugins.UnitTest#reset()} whereby the reset method
     * used to reset the test environment fails to reset commandExecutionHook field and thus prevents multiple tests from running.
     * <p/>
     * This class fixes this in a very hacky way and should be abandoned as tenacity is migrated to new hystrix that fixes it.
     * Pull request has been merged, but new version has not been released yet:
     * https://github.com/Netflix/Hystrix/pull/240
     */
    private static void resetCommandExecutionHook() {
        try {
            Field commandExecutionHookField = HystrixPlugins.class.getDeclaredField("commandExecutionHook");
            commandExecutionHookField.setAccessible(true);
            HystrixPlugins jvmInstance = HystrixPlugins.getInstance();
            AtomicReference<HystrixCommandExecutionHook> commandExecutionHookRef =
                    (AtomicReference<HystrixCommandExecutionHook>) commandExecutionHookField.get(jvmInstance);
            commandExecutionHookRef.set(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw Throwables.propagate(e);
        }
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