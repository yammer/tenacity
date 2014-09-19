0.4.0
-----
Changed `TenacityBundle` to be a `TenacityConfiguredBundle` and streamlined the tenacity-application integration process. This means:
- when declaring `TenacityPropertyKey` instances, there is access to the configuration class
- manual registering of configurations is no longer necessary.

0.3.6
-----
* Hystrix 1.3.18

0.3.5
-------------
* Adding 95/98 to the latencies. 
* Fixed bug where we are no longer publishing HystrixThreadPool metrics.

0.3.4
-------------
* Adding 99.9 to the latencies. Hystrix by default maxes out at 99.5 which is not consistent with Coda Hale metrics

0.3.3
--------------
* Dropwizard 0.7.1
* Force hystrix-codahale to depend on metrics 3.0.2, until [pull request 279](https://github.com/Netflix/Hystrix/pull/279) is merged
* Upgrade to findbugs 2.5.4
* Merged pull request 8. This adds a bunch of tests to test failure behavioral differences between execute()/queue()/observe()

0.3.2
-----
* Under high contention it's possible for HystrixThreadPoolMetrics to be associated with an incorrect TheadPoolExecutor.
This manifests itself as showing the incorrect metrics because the executor being used by Hystrix is different from the one
being used to supply metrics. As a temporary fix we now ensure that for any given ThreadPoolKey we only ever construct one pool.
This will be removed once it's fixed in upstream Hystrix. [pull request 270](https://github.com/Netflix/Hystrix/pull/270)

0.3.1
-----
* tenacity-client incorrectly captured metrics
* Hystrix 1.3.16
* maven-enforcer-plugin

0.3.0
--------------
* Supporting dropwizard 0.7.0
* 0.2.x now will only have fixes for the deprecated dropwizard <0.7.0



Dropwizard 0.6.x Releases
=========================
0.2.8
-----
* Hystrix 1.3.18

0.2.7
-------------
* Adding 95/98 to the latencies. 
* Fixed bug where we are no longer publishing HystrixThreadPool metrics.

0.2.6
-------------
* Adding 99.9 to the latencies. Hystrix by default maxes out at 99.5 which is not consistent with Coda Hale metrics

0.2.5
-----
* Under high contention it's possible for HystrixThreadPoolMetrics to be associated with an incorrect TheadPoolExecutor.
This manifests itself as showing the incorrect metrics because the executor being used by Hystrix is different from the one
being used to supply metrics. As a temporary fix we now ensure that for any given ThreadPoolKey we only ever construct one pool.
This will be removed once it's fixed in upstream Hystrix. [pull request 270](https://github.com/Netflix/Hystrix/pull/270)

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
