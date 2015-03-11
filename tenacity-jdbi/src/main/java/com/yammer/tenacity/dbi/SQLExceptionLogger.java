package com.yammer.tenacity.dbi;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.netflix.hystrix.HystrixInvokableInfo;
import com.yammer.tenacity.core.logging.ExceptionLogger;

import java.sql.SQLException;

public class SQLExceptionLogger extends ExceptionLogger<SQLException> {

    private final Meter SQL_ERROR;

    public SQLExceptionLogger(MetricRegistry registry) {
        this.SQL_ERROR = registry.meter(MetricRegistry.name(SQLExceptionLogger.class, "sql-errors", "error"));
    }
    
    @Override
    protected <T> void logException(SQLException exception, HystrixInvokableInfo<T> command) {
        SQL_ERROR.mark();
        logSQLException(exception, command);
    }

    <T> void logSQLException(SQLException exception, HystrixInvokableInfo<T> command) {
        for (Throwable throwable : exception) {
            logger.error("SQL problem running command: {}:{}", command.getCommandKey(), command.getClass().getSimpleName(), throwable);
        }
    }
}
