package com.yammer.tenacity.tests.logging;

import com.google.common.collect.Lists;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.logging.ExceptionLogger;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.testing.TenacityTest;
import com.yammer.tenacity.tests.DependencyKey;
import com.yammer.tenacity.tests.TenacityFailingCommand;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore("Can't run this with other test classes as the CommandExecutionHook will already have been set")
public class ExceptionLoggingCommandHookIntegrationTest extends TenacityTest {

    // Using a dummy exception logger that's statically defined as Hystrix only lets you set the environment hook once
    private static final DummyExceptionLogger exceptionLogger = new DummyExceptionLogger();

    @BeforeClass
    public static void setUpExceptionLogger() throws Exception {
        HystrixPlugins.getInstance().registerCommandExecutionHook(new ExceptionLoggingCommandHook(exceptionLogger));
    }

    @Before
    public void setup() {
        exceptionLogger.resetExceptions();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void loggerLogsOnExpectedException() throws Exception {
        final HystrixCommand<String> failingCommand = new TenacityFailingCommand();

        failingCommand.execute();

        final List<RuntimeException> loggedExceptions = exceptionLogger.getLoggedExceptions();
        assertEquals(1, loggedExceptions.size());
        assertTrue(loggedExceptions.get(0).getClass().equals(RuntimeException.class));
    }

    @Test
    public void loggerDoesntLogIfItsNotExpected() throws Exception {
        final HystrixCommand<String> failingCommand = new TenacityFailingWithIOException();

        failingCommand.execute();

        final List<RuntimeException> loggedExceptions = exceptionLogger.getLoggedExceptions();
        assertTrue(loggedExceptions.isEmpty());
    }

    static final class DummyExceptionLogger extends ExceptionLogger<RuntimeException> {

        private List<RuntimeException> loggedExceptions;

        DummyExceptionLogger() {
            this.loggedExceptions = Lists.newArrayList();
        }

        @Override
        protected <T> void logException(RuntimeException exception, HystrixCommand<T> commandInstance) {
            loggedExceptions.add(exception);
        }

        public List<RuntimeException> getLoggedExceptions() {
            return loggedExceptions;
        }

        public void resetExceptions() {
            loggedExceptions = Lists.newArrayList();
        }
    }

    static final class TenacityFailingWithIOException extends TenacityCommand<String> {

        public TenacityFailingWithIOException(){
            super(DependencyKey.EXAMPLE);
        }

        @Override
        protected String run() throws Exception {
            throw new IOException("purposely failing");
        }

        @Override
        protected String getFallback() {
            return "fallback";
        }
    }
}
