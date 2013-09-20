package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.testing.TenacityTest;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ManagedConcurrencyStrategyTest extends TenacityTest {
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
