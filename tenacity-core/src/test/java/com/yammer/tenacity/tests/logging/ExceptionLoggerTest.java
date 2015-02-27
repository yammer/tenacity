package com.yammer.tenacity.tests.logging;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.netflix.hystrix.HystrixInvokableInfo;
import com.yammer.tenacity.core.logging.ExceptionLogger;
import com.yammer.tenacity.tests.TenacityFailingCommand;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class ExceptionLoggerTest {

    private final TenacityFailingCommand failingCommand = new TenacityFailingCommand();
    private final RuntimeException validException = new RuntimeException();
    private final IllegalStateException validSubException = new IllegalStateException();
    private final IOException invalidException = new IOException();
    private DummyExceptionLogger exceptionLogger;

    @Before
    public void setUp() throws Exception {
        exceptionLogger = new DummyExceptionLogger();
    }

    @Test
    public void canLogSameTypeException() throws Exception {
        assertTrue(exceptionLogger.canHandleException(validException));
    }

    @Test
    public void canLogSubTypeException() throws Exception {
        assertTrue(exceptionLogger.canHandleException(validSubException));
    }

    @Test
    public void cannotLogInvalidTypeException() throws Exception {
        assertFalse(exceptionLogger.canHandleException(invalidException));
    }

    @Test(expected = IllegalStateException.class)
    public void throwsIllegalStateIfLogInvalidType() throws Exception {
        exceptionLogger.log(invalidException, failingCommand);
    }

    @Test
    public void logsExceptionOfSameType() throws Exception {
        exceptionLogger.log(validException, failingCommand);
        assertEquals(ImmutableList.of(validException), exceptionLogger.getLoggedExceptions());
    }

    @Test
    public void logsExceptionOfSubType() throws Exception {
        exceptionLogger.log(validSubException, failingCommand);
        assertEquals(ImmutableList.of(validSubException), exceptionLogger.getLoggedExceptions());
    }

    static final class DummyExceptionLogger extends ExceptionLogger<RuntimeException> {

        private final List<RuntimeException> loggedExceptions;

        DummyExceptionLogger() {
            this.loggedExceptions = Lists.newArrayList();
        }

        @Override
        protected <T> void logException(RuntimeException exception, HystrixInvokableInfo<T> commandInstance) {
            loggedExceptions.add(exception);
        }

        public List<RuntimeException> getLoggedExceptions() {
            return loggedExceptions;
        }
    }
}
