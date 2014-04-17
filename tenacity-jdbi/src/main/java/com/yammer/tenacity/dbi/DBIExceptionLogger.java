package com.yammer.tenacity.dbi;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import com.netflix.hystrix.HystrixCommand;
import com.yammer.tenacity.core.logging.ExceptionLogger;
import org.skife.jdbi.v2.exceptions.DBIException;
import java.sql.SQLException;

public class DBIExceptionLogger extends ExceptionLogger<DBIException> {

    private final Meter DBI_ERRORS; 
    private final SQLExceptionLogger sqlExceptionLogger;

    public DBIExceptionLogger(MetricRegistry registry) {
        this(registry, new SQLExceptionLogger(registry));
    }

    public DBIExceptionLogger(MetricRegistry registry, SQLExceptionLogger sqlExceptionLogger) {
        this.sqlExceptionLogger = sqlExceptionLogger;
        this.DBI_ERRORS = registry.meter(MetricRegistry.name(DBIExceptionLogger.class, "dbi-errors", "error"));
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected <T> void logException(DBIException exception, HystrixCommand<T> command) {
        DBI_ERRORS.mark();
        final Throwable cause = Throwables.getRootCause(exception);
        if (cause instanceof SQLException) {
            sqlExceptionLogger.logSQLException((SQLException) cause, command);
        } else {
            logger.error("DBI problem running command: {}:{}", command.getCommandKey(), command.getClass().getSimpleName(), exception);
        }
    }

}
