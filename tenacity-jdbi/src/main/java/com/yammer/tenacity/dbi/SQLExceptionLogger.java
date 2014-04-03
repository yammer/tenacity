package com.yammer.tenacity.dbi;

import com.netflix.hystrix.HystrixCommand;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.tenacity.core.logging.ExceptionLogger;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class SQLExceptionLogger extends ExceptionLogger<SQLException> {

    private static final Meter SQL_ERROR = Metrics.newMeter(SQLExceptionLogger.class, "sql-errors", "error", TimeUnit.SECONDS);

    @Override
    protected <T> void logException(SQLException exception, HystrixCommand<T> command) {
        SQL_ERROR.mark();
        logSQLException(exception, command);
    }

    <T> void logSQLException(SQLException exception, HystrixCommand<T> command) {
        for (Throwable throwable : exception) {
            logger.error("SQL problem running command: {}:{}", command.getCommandKey(), command.getClass().getSimpleName(), throwable);
        }
    }
}
