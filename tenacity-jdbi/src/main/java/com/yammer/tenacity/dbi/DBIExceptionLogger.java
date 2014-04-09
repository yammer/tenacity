package com.yammer.tenacity.dbi;

import com.google.common.base.Throwables;
import com.netflix.hystrix.HystrixCommand;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.tenacity.core.logging.ExceptionLogger;
import org.skife.jdbi.v2.exceptions.DBIException;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class DBIExceptionLogger extends ExceptionLogger<DBIException> {

    private static final Meter DBI_ERRORS = Metrics.newMeter(DBIExceptionLogger.class, "dbi-errors", "error", TimeUnit.SECONDS);

    private final SQLExceptionLogger sqlExceptionLogger;

    public DBIExceptionLogger() {
        this(new SQLExceptionLogger());
    }

    public DBIExceptionLogger(SQLExceptionLogger sqlExceptionLogger) {
        this.sqlExceptionLogger = sqlExceptionLogger;
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
