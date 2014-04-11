0.2.3
-----
Created ExceptionLoggingCommandHook which passes thrown exceptions to the appropriate, registered ExceptionLogger
Added HystrixCommandExecutionHook to TenacityBundleBuilder
Created DefaultExceptionLogger, which just logs everything
Created tenacity-jdbi module to house DBIExceptionLogger and SQLExceptionLogger

0.2.2
------
Upgrade Hystrix to 1.3.13

0.2.1
--------------
Upgrade Hystrix to 1.3.9


0.2.0
-----
Initial release
