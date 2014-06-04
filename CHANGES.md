0.3.2
-----
* Under high contention it's possible for HystrixThreadPoolMetrics to be associated with an incorrect TheadPoolExecutor.
This manifests itself as showing the incorrect metrics because the executor being used by Hystrix is different from the one
being used to supply metrics. As a temporary fix we now ensure that for any given ThreadPoolKey we only ever construct one pool.
This will be removed once it's fixed in upstream Hystrix.

0.3.1
-----
* tenacity-client incorrectly captured metrics
* Hystrix 1.3.16
* maven-enforcer-plugin

0.3.0
--------------
* Supporting dropwizard 0.7.0
* 0.2.x now will only have fixes for the deprecated dropwizard <0.7.0

0.2.4
-----
* Hystrix 1.3.15
* Generics lacking from some classes.

0.2.3
-----
* Created ExceptionLoggingCommandHook which passes thrown exceptions to the appropriate, registered ExceptionLogger
* Added HystrixCommandExecutionHook to TenacityBundleBuilder
* Created DefaultExceptionLogger, which just logs everything
* Created tenacity-jdbi module to house DBIExceptionLogger and SQLExceptionLogger

0.2.2
------
* Upgrade Hystrix to 1.3.13

0.2.1
--------------
* Upgrade Hystrix to 1.3.9


0.2.0
-----
* Initial release
