package com.yammer.tenacity.tests.logging;

import com.netflix.hystrix.HystrixCommand;
import com.yammer.tenacity.core.logging.ExceptionLogger;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.testing.TenacityTest;
import com.yammer.tenacity.tests.TenacityFailingCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionLoggingCommandHookTest extends TenacityTest {

    private final HystrixCommand<String> failedCommand = new TenacityFailingCommand();
    private final Exception exception = new Exception();

    @Mock
    private ExceptionLogger<RuntimeException> firstLogger;

    @Mock
    private ExceptionLogger<RuntimeException> secondLogger;

    @Mock
    private ExceptionLogger<RuntimeException> thirdLogger;

    private ExceptionLoggingCommandHook hook;

    @Before
    public void setUp() throws Exception {
        hook = new ExceptionLoggingCommandHook(firstLogger, secondLogger, thirdLogger);
    }

    @Test
    public void loggingInFirstLoggerBlocksOthers() throws Exception {
        when(firstLogger.canHandleException(exception)).thenReturn(true);

        assertEquals(exception, hook.onRunError(failedCommand, exception));

        verify(firstLogger, times(1)).log(exception, failedCommand);
        verify(secondLogger, times(0)).canHandleException(exception);
        verify(secondLogger, times(0)).log(exception, failedCommand);
        verify(thirdLogger, times(0)).canHandleException(exception);
        verify(thirdLogger, times(0)).log(exception, failedCommand);
    }

    @Test
    public void loggingInSecondLoggerBlocksThird() throws Exception {
        when(firstLogger.canHandleException(exception)).thenReturn(false);
        when(secondLogger.canHandleException(exception)).thenReturn(true);

        assertEquals(exception, hook.onRunError(failedCommand, exception));

        verify(firstLogger, times(0)).log(exception, failedCommand);
        verify(secondLogger, times(1)).log(exception, failedCommand);
        verify(thirdLogger, times(0)).canHandleException(exception);
        verify(thirdLogger, times(0)).log(exception, failedCommand);
    }

    @Test
    public void logWithThirdLogger() throws Exception {
        when(firstLogger.canHandleException(exception)).thenReturn(false);
        when(secondLogger.canHandleException(exception)).thenReturn(false);
        when(thirdLogger.canHandleException(exception)).thenReturn(true);

        assertEquals(exception, hook.onRunError(failedCommand, exception));

        verify(firstLogger, times(0)).log(exception, failedCommand);
        verify(secondLogger, times(0)).log(exception, failedCommand);
        verify(thirdLogger, times(1)).log(exception, failedCommand);
    }

    @Test
    public void noAvailableLogger() throws Exception {
        when(firstLogger.canHandleException(exception)).thenReturn(false);
        when(secondLogger.canHandleException(exception)).thenReturn(false);
        when(thirdLogger.canHandleException(exception)).thenReturn(false);

        assertEquals(exception, hook.onRunError(failedCommand, exception));

        verify(firstLogger, times(0)).log(exception, failedCommand);
        verify(secondLogger, times(0)).log(exception, failedCommand);
        verify(thirdLogger, times(0)).log(exception, failedCommand);
    }
}
