package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ManagedConcurrencyStrategyTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

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
        assertThat(new SimpleCommand().execute(), is(equalTo(Key.KEY.toString())));
        assertThat(new SimpleCommand().queue().get(), is(equalTo(Key.KEY.toString())));
    }
}
