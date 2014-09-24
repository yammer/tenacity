package com.yammer.tenacity.tests.logging;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.logging.DefaultExceptionLogger;
import com.yammer.tenacity.core.logging.ExceptionLogger;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.testing.TenacityTestRule;
import com.yammer.tenacity.tests.DependencyKey;
import com.yammer.tenacity.tests.TenacityFailingCommand;
import io.dropwizard.auth.AuthenticationException;
import org.junit.*;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ExceptionLoggingCommandHookIntegrationTest {

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    // Using a dummy exception logger that's statically defined as Hystrix only lets you set the environment hook once
    private static final DummyExceptionLogger exceptionLogger = new DummyExceptionLogger();

    @Before
    public void setup() {
        exceptionLogger.resetExceptions();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void loggerLogsOnExpectedException() throws Exception {
        HystrixPlugins.getInstance().registerCommandExecutionHook(new ExceptionLoggingCommandHook(exceptionLogger));
        final HystrixCommand<String> failingCommand = new TenacityFailingCommand();

        failingCommand.execute();

        final List<RuntimeException> loggedExceptions = exceptionLogger.getLoggedExceptions();
        assertEquals(1, loggedExceptions.size());
        assertTrue(loggedExceptions.get(0).getClass().equals(RuntimeException.class));
    }

    @Test
    public void loggerDoesntLogIfItsNotExpected() throws Exception {
        HystrixPlugins.getInstance().registerCommandExecutionHook(new ExceptionLoggingCommandHook(exceptionLogger));
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

    private static class AlwaysShortCircuit extends HystrixCommand<String> {
        private AlwaysShortCircuit() {
            super(HystrixCommand.Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withCircuitBreakerForceOpen(true)));
        }

        @Override
        protected String run() throws Exception {
            fail();
            throw new RuntimeException();
        }

        @Override
        protected String getFallback() {
            return "";
        }
    }

    @Test
    public void shouldNotLogWhenShortCircuited() {
        final DefaultExceptionLogger defaultExceptionLogger = spy(new DefaultExceptionLogger());
        HystrixPlugins.getInstance().registerCommandExecutionHook(new ExceptionLoggingCommandHook(defaultExceptionLogger));

        try {
            new AlwaysShortCircuit().execute();
        } catch (HystrixRuntimeException err) {
            assertFalse(Iterables.isEmpty(
                    Iterables.filter(Throwables.getCausalChain(err), AuthenticationException.class)));
        }

        verifyZeroInteractions(defaultExceptionLogger);
    }
}
