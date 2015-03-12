package com.yammer.tenacity.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;
import rx.Observable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests that the cause for returning a fallback is available from within the fallback execution.
 * <p/>
 * There are 4 possible causes for returning a fallback:
 * - TenacityCommand short circuited
 * - TenacityCommand.run() timed out
 * - TenacityCommand.run() threw an Exception
 * - Tenacity thread pool rejection
 * <p/>
 * Note: When testing TenacityCommand.queue(), Future.get() should not be used to force the command to complete or
 * it may perform the same as TenacityCommand.execute(); spin on Future.isDone(), then call Future.get().
 */
public class TenacityCommandFailureCauseTest {
    /*
        Timed out failure tests
     */

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void timedOutAvailableInGetFallbackUsingExecute() {
        setUpTenacityCommand(2, 10);
        assertTrue(timedOutCommand(20).execute());
    }

    @Test(timeout = 1000)
    public void timeoutIsNotObeyedUsingQueue() throws Exception {
        setUpTenacityCommand(2, 10);
        final Future<Boolean> result = timedOutCommand(20).queue();
        while (!result.isDone()) {
            Thread.sleep(10);
        }
        assertFalse(result.get());
    }

    @Test
    public void timedOutAvailableInGetFallbackUsingObserve() {
        setUpTenacityCommand(2, 10);
        final Observable<Boolean> result = timedOutCommand(20).observe();
        assertTrue(result.toBlocking().single());
    }


    /*
        Exception thrown failure tests
     */

    @Test
    public void thrownExceptionAvailableInGetFallbackUsingExecute() {
        setUpTenacityCommand(2, 100);
        assertTrue(exceptionCommand().execute());
    }

    @Test(timeout = 1000)
    public void thrownExceptionAvailableInGetFallbackUsingQueue() throws Exception {
        setUpTenacityCommand(2, 100);
        final Future<Boolean> result = exceptionCommand().queue();
        while (!result.isDone()) {
            Thread.sleep(10);
        }
        assertTrue(result.get());
    }

    @Test
    public void thrownExceptionAvailableInGetFallbackUsingObserve() {
        setUpTenacityCommand(2, 100);
        final Observable<Boolean> result = exceptionCommand().observe();
        assertTrue(result.toBlocking().single());
    }


    /*
        Short circuited failure tests
     */

    @SuppressWarnings("StatementWithEmptyBody")
    @Test(timeout = 1000)
    public void shortCircuitedAvailableInGetFallbackUsingExecute() {
        setUpTenacityCommand(2, 100);
        final TenacityCommand<?> exceptionCommand = exceptionCommand();
        exceptionCommand.execute();
        while (!exceptionCommand.isCircuitBreakerOpen());
        assertTrue(shortCircuitedCommand().execute());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Test(timeout = 1000)
    public void shortCircuitedAvailableInGetFallbackUsingQueue() throws Exception {
        setUpTenacityCommand(2, 100);
        final TenacityCommand<?> exceptionCommand = exceptionCommand();
        exceptionCommand.execute();
        while (!exceptionCommand.isCircuitBreakerOpen()) ;
        final Future<Boolean> result = shortCircuitedCommand().queue();
        while (!result.isDone()) {
            Thread.sleep(10);
        }
        assertTrue(result.get());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Test(timeout = 1000)
    public void shortCircuitedAvailableInGetFallbackUsingObserve() {
        setUpTenacityCommand(2, 100);
        final TenacityCommand<?> exceptionCommand = exceptionCommand();
        exceptionCommand.execute();
        while (!exceptionCommand.isCircuitBreakerOpen()) ;
        final Observable<Boolean> result = shortCircuitedCommand().observe();
        assertTrue(result.toBlocking().single());
    }


    /*
        Thread pool rejection failure tests
     */

    @Test(timeout = 1000)
    public void threadPoolRejectionAvailableInGetFallbackUsingExecute() throws Exception {
        setUpTenacityCommand(1, 100);
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return sleepCommand(80).execute();
            }
        });
        final List<Callable<Boolean>> rejectCommands = Lists.newArrayListWithExpectedSize(10);
        for (int i = 0; i < 5; i++) {
            rejectCommands.add(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return threadPoolRejectionCommand().execute();
                }
            });
        }
        final Collection<Future<Boolean>> results = executorService.invokeAll(rejectCommands);
        boolean rejectionFound = false;
        for (final Future<Boolean> future : results) {
            if (future.get()) {
                rejectionFound = true;
            }
        }
        executorService.shutdownNow();
        assertTrue(rejectionFound);
    }

    @Test(timeout = 1000)
    public void threadPoolRejectionAvailableInGetFallbackUsingQueue() throws Exception {
        setUpTenacityCommand(1, 100);
        sleepCommand(80).queue();
        final List<Future<Boolean>> results = Lists.newArrayListWithExpectedSize(5);
        for (int i = 0; i < 5; i++) {
            results.add(threadPoolRejectionCommand().queue());
        }
        boolean rejectionFound = false;
        for (final Future<Boolean> future : results) {
            while (!future.isDone()) {
                Thread.sleep(10);
            }
            if (future.get()) {
                rejectionFound = true;
            }
        }
        assertTrue(rejectionFound);
    }

    @Test
    public void threadPoolRejectionAvailableInGetFallbackUsingObserve() {
        setUpTenacityCommand(1, 100);
        sleepCommand(80).queue();
        final List<Observable<Boolean>> results = Lists.newArrayListWithExpectedSize(5);
        for (int i = 0; i < 5; i++) {
            results.add(threadPoolRejectionCommand().observe());
        }
        boolean rejectionFound = false;
        for (final Observable<Boolean> observable : results) {
            if (observable.toBlocking().single()) {
                rejectionFound = true;
            }
        }
        assertTrue(rejectionFound);
    }


    /*
        Test helpers
     */

    private TenacityCommand<Boolean> sleepCommand(final int sleepMs) {
        return new TenacityCommand<Boolean>(DependencyKey.EXAMPLE) {
            @Override
            protected Boolean run() throws Exception {
                Thread.sleep(sleepMs);
                return true;
            }

            @Override
            protected Boolean getFallback() {
                return false;
            }
        };
    }

    private TenacityCommand<Boolean> timedOutCommand(final int sleepMs) {
        return new TenacityCommand<Boolean>(DependencyKey.EXAMPLE) {
            @Override
            protected Boolean run() throws Exception {
                Thread.sleep(sleepMs);
                return false;
            }

            @Override
            protected Boolean getFallback() {
                return isResponseTimedOut();
            }
        };
    }

    private TenacityCommand<Boolean> exceptionCommand() {
        return new TenacityCommand<Boolean>(DependencyKey.EXAMPLE) {
            @Override
            protected Boolean run() throws Exception {
                throw new TenacityTestException();
            }

            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            @Override
            protected Boolean getFallback() {
                final Throwable thrown = getFailedExecutionException();
                return thrown != null && thrown.getClass().equals(TenacityTestException.class);
            }

            class TenacityTestException extends Exception {
            }
        };
    }

    private TenacityCommand<Boolean> shortCircuitedCommand() {
        return new TenacityCommand<Boolean>(DependencyKey.EXAMPLE) {
            @Override
            protected Boolean run() throws Exception {
                return false;
            }

            @Override
            protected Boolean getFallback() {
                return isResponseShortCircuited();
            }
        };
    }

    private TenacityCommand<Boolean> threadPoolRejectionCommand() {
        return new TenacityCommand<Boolean>(DependencyKey.EXAMPLE) {
            @Override
            protected Boolean run() {
                return false;
            }

            @Override
            protected Boolean getFallback() {
                return isResponseRejected();
            }
        };
    }

    private void setUpTenacityCommand(int poolSize, int timeout) {
        final ThreadPoolConfiguration poolConfig = new ThreadPoolConfiguration();
        poolConfig.setThreadPoolCoreSize(poolSize);
        final CircuitBreakerConfiguration circuitConfig = new CircuitBreakerConfiguration();
        circuitConfig.setErrorThresholdPercentage(1);
        circuitConfig.setRequestVolumeThreshold(1);
        new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(
                        DependencyKey.EXAMPLE, new TenacityConfiguration(
                                poolConfig, circuitConfig, timeout
                        )
                ),
                new BreakerboxConfiguration()
        ).register();
    }
}
