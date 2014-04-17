Tenacity [![Build Status](https://travis-ci.org/yammer/tenacity.png)](https://travis-ci.org/yammer/tenacity)
========

Tenacity is [Dropwizard](http://www.dropwizard.io)+[Hystrix](https://github.com/Netflix/Hystrix).

[Dropwizard](http://www.dropwizard.io) is a framework for building REST services. [Hystrix](https://github.com/Netflix/Hystrix) is a resiliency library from [Netflix](https://github.com/Netflix) and [Ben Christensen](https://github.com/benjchristensen).

[Hystrix's](https://github.com/Netflix/Hystrix) goals are to:

1. Stop cascading failures.
2. Fail-fast and rapidly recover.
3. Reduce mean-time-to-discovery (with dashboards)
4. Reduce mean-time-to-recovery (with dynamic configuration)

Tenacity makes [Hystrix](https://github.com/Netflix/Hystrix) dropwizard-friendly and for dropwizard-developers to quickly leverage the benefits of Hystrix.

1. Uses dropwizard-bundles for bootstrapping: property strategies, metrics, dynamic configuration, and some resource endpoints (e.g. for dashboards).
2. Dropwizard-configuration style (YAML) for dependencies.
3. Abstractions to clearly configure a dependency operation (`TenacityCommand<ReturnType>`).
4. Ability to unit-test Hystrix: Resets static state held by Hystrix (metrics, counters, etc.). Increases rate at which a concurrent thread updates metrics.
5. Publishes measurements via [Metrics](https://github.com/codahale/metrics).

*Tenacity is meant to be used with [Breakerbox](https://github.com/yammer/breakerbox) which adds real-time visualization of metrics and dynamic configuration. This isn't open sourced yet, but will be soon.*

Modules
-------

-   `tenacity-core`:            The building blocks to quickly use Hystrix within the context of Dropwizard.
-   `tenacity-core-legacy`:     Support for legacy versions of Dropwizard. Currently there is a conflict with <0.6.0 versions (around Bundles).
-   `tenacity-client`:          Client for consuming the resources that `tenacity-core` adds.
-   `tenacity-testing`:         `TenacityTest` allows for easier unit testing. Resets internal state of Hystrix.
-   `tenacity-jdbi`:            Pulls in dropwizard-jdbi and provides a DBIExceptionLogger and SQLExceptionLogger to be used with the ExceptionLoggingCommandHook.

How To Use
==========

Here is a sample `TenacityCommand` that always succeeds:

```java
public class AlwaysSucceed extends TenacityCommand<String> {
    public AlwaysSucceed() {
        super(DependencyKey.ALWAYS_SUCCEED);
    }

    @Override
    protected String run() throws Exception {
        return "value";
    }
}
```

A quick primer on the way to use a `TenacityCommand` if you are not familiar with [Hystrix's execution model](https://github.com/Netflix/Hystrix/wiki/How-To-Use#wiki-Synchronous-Execution):

Synchronous Execution
---------------------
```java
AlwaysSucceed command = new AlwaysSucceed();
String result = command.execute();
```

This executes the command synchronously but through the protection of a `Future.get(configurableTimeout)`.

Asynchronous Execution
----------------------

```java
Future<String> futureResult = command.queue();
```

Reactive Execution
------------------------------

```java
Observable<String> observable = new AlwaysSucceed().observe();
```

Fallbacks
---------

When execution fails, it is possible to gracefully degrade with the use of [fallbacks](https://github.com/Netflix/Hystrix/wiki/How-To-Use#wiki-Fallback).

Execution Flow
--------------

![Alt text](https://raw.github.com/wiki/Netflix/Hystrix/images/hystrix-flow-chart-original.png)


TenacityCommand Constructor Arguments
-------------------------------------

Earlier we saw:

```java
public class AlwaysSucceed extends TenacityCommand<String> {
    public AlwaysSucceed() {
        super(DependencyKey.ALWAYS_SUCCEED);
    }
    ...
}
```

The arguments are:

1. `commandKey`: This creates a circuit-breaker, threadpool, and also the identifier that will be used in dashboards.
This should be your implementation of the `TenacityPropertyKey` interface.

*It is possible to create multiple circuit-breakers that leverage a single threadpool, but for simplicity we are not allowing that type of configuration.*


How to add Tenacity to your Dropwizard Service
----------

1. To leverage within dropwizard first at the following to your `pom.xml`:

        <dependency>
            <groupId>com.yammer.tenacity</groupId>
            <artifactId>tenacity-core</artifactId>
            <version>0.2.2</version>
        </dependency>


2. Enumerate your dependencies. These will eventually be used as global identifiers in dashboards. We have found that it works best for us
when you include the service and the external dependency at a minimum. Here is an example of `completie`'s dependencies. Note we also
shave down some characters to save on space, again for UI purposes. In addition, you'll need to have an implementation of a `TenacityPropertyKeyFactory` which you can see an example of below.

    ```java
    public enum CompletieDependencyKeys implements TenacityPropertyKey {
        CMPLT_PRNK_USER, CMPLT_PRNK_GROUP, CMPLT_PRNK_SCND_ORDER, CMPLT_PRNK_NETWORK,
        CMPLT_TOKIE_AUTH,
        CMPLT_TYRANT_AUTH,
        CMPLT_WHVLL_PRESENCE
    }
    ```
    ```java
    public class CompletieDependencyKeyFactory implements TenacityPropertyKeyFactory {
        @Override
        public TenacityPropertyKey from(String value) {
            return CompletieDependencyKeys.valueOf(value.toUpperCase());
        }
    }
    ```

3. Then make sure you add the bundle in your `Service` and register your custom tenacity properties. Here we made use of a helper class
to register properties given a `CompletieConfiguration`. This is helpful when you might need to register custom properties from multiple locations
such as application and testing code. Note the specialized class uses `TenacityPropertyRegister` which takes a: `Map<TenacityPropertyKey, TenacityConfiguration>` type.

    ```java
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        ...
        bootstrap.addBundle(TenacityBundleBuilder
                                            .newBuilder()
                                            .propertyKeyFactory(new CompletieDependencyKeyFactory())
                                            .propertyKeys(CompletieDependencyKeys.values())
                                            .build();
        ...
    }

    @Override
    public void run(CompletieConfiguration configuration, Environment environment) throws Exception {
         new CompletieTenacityPropertyRegister(configuration).register();
    }
    ```

    ```java
    public class CompletieTenacityPropertyRegister {
        private final CompletieConfiguration configuration;

        public CompletieTenacityPropertyRegister(CompletieConfiguration configuration) {
            this.configuration = configuration;
        }

        public void register() {
            final ImmutableMap.Builder<TenacityPropertyKeys, TenacityConfiguration> builder = ImmutableMap.builder();

            builder.put(CompletieDependencyKeys.CMPLT_PRNK_USER, configuration.getRanking().getHystrixUserConfig());
            builder.put(CompletieDependencyKeys.CMPLT_PRNK_GROUP, configuration.getRanking().getHystrixGroupConfig());
            builder.put(CompletieDependencyKeys.CMPLT_PRNK_SCND_ORDER, configuration.getRanking().getHystrixSecondOrderConfig());
            builder.put(CompletieDependencyKeys.CMPLT_PRNK_NETWORK, configuration.getRanking().getHystrixNetworkConfig());
            builder.put(CompletieDependencyKeys.CMPLT_TOKIE_AUTH, configuration.getAuthentication().getHystrixConfig());
            builder.put(CompletieDependencyKeys.CMPLT_WHVLL_PRESENCE, configuration.getPresence().getHystrixConfig());

            new TenacityPropertyRegister(builder.build(), configuration.getBreakerboxConfiguration()).register();
        }
    }
    ```

4. Use `TenacityCommand` to select which custom tenacity configuration you want to use.

    ```java
    public class CompletieDependencyOnTokie extends TenacityCommand<String> {
        public CompletieDependencyOnTokie() {
            super(CompletieDependencyKeys.CMPLT_TOKIE_AUTH);
        }
        ...
    }
    ```

5. When testing use the `tenacity-testing` module. This registers appropriate custom publishers/strategies, clears global `Archaius` configuration state (Hystrix uses internally to manage configuration),
and tweaks threads that calculate metrics which influence circuit breakers to update a more frequent interval. Simply extend the `TenacityTest` helper.

        <dependency>
            <groupId>com.yammer.tenacity</groupId>
            <artifactId>tenacity-testing</artifactId>
            <version>0.2.2</version>
            <scope>test</scope>
        </dependency>

6. Last is to actually configure your dependencies once they are wrapped with `TenacityCommand`.


Configuration
=============

Once you have identified your dependencies you need to configure them appropriately. Here is the basic structure of a single
`TenacityConfiguration` that may be leverage multiple times through your service configuration:

Defaults
--------
```yaml
executionIsolationThreadTimeoutInMillis: 1000
threadpool:
    threadPoolCoreSize: 10
    keepAliveTimeMinutes: 1
    maxQueueSize: -1
    queueSizeRejectionThreshold: 5
    metricsRollingStatisticalWindowInMilliseconds: 10000
    metricsRollingStatisticalWindowBuckets: 10
circuitBreaker:
    requestVolumeThreshold: 20
    errorThresholdPercentage: 50
    sleepWindowInMillis: 5000
    metricsRollingStatisticalWindowInMilliseconds: 10000
    metricsRollingStatisticalWindowBuckets: 10
```

The following two are the most important and you can probably get by just fine by defining just these two and leveraging the
defaults.

-   `executionIsolationThreadTimeoutInMillis`: How long the entire dependency command should take.
-   `threadPoolCoreSize`: Self explanatory.

Here are the rest of the descriptions:

-   `keepAliveTimeMinutes`: Thread keepAlive time in the thread pool.
-   `maxQueueSize`: -1 uses a `SynchronousQueue`. Anything >0 leverages a `BlockingQueue` and enables the `queueSizeRejectionThreshold` variable.
-   `queueSizeRejectionThreshold`: Disabled when using -1 for `maxQueueSize` otherwise self explanatory.
-   `requestVolumeThreshold`: The minimum number of requests that need to be received within the `metricsRollingStatisticalWindowInMilliseconds` in order to open a circuit breaker.
-   `errorThresholdPercentage`: The percentage of errors needed to trip a circuit breaker. In order for this to take effect the `requestVolumeThreshold` must first be satisfied.
-   `sleepWindowInMillis`: How long to keep the circuit breaker open, before trying again.

These are recommended to be left alone unless you know what you're doing:

-   `metricsRollingStatisticalWindowInMilliseconds`: How long to keep around metrics for calculating rates.
-   `metricsRollingStatisticalWindowBuckets`: How many different metric windows to keep in memory.

Once you are done configuring your Tenacity dependencies. Don't forget to tweak the necessary connect/read timeouts on HTTP clients.
We have some suggestions for how you go about this in the Equations section.

Breakerbox
----------
One of the great things about Tenacity is the ability to aid in the reduction of mean-time-to-discovery and mean-time-to-recovery for issues. This is available through a separate service [Breakerbox](https://github.com/yammer/breakerbox).

[Breakerbox](https://github.com/yammer/breakerbox) is a central dashboard and an on-the-fly configuration tool for Tenacity. In addition to the per-tenacity-command configurations shown above this configuration piece let's you define where and how often
to check for newer configurations.

```yaml
breakerbox:
  urls: http://breakerbox.yourcompany.com:8080/archaius/{service}
  initialDelay: 0s
  delay: 60s
```

-   `urls` is a list of comma-deliminated list of urls for where to pull tenacity configurations. This will pull override configurations for all dependency keys for requested service.
-   `initialDelay` how long before the first poll for newer configuration executes.
-   `delay` the ongoing schedule to poll for newer configurations.

![Breakerbox Dashboard](http://yammer.github.io/tenacity/breakerbox_latest.png)
![Breakerbox Configure](http://yammer.github.io/tenacity/breakerbox_configure.png)

Configuration Hierarchy Order
-----------------------------

Configurations can happen in a lot of different spots so it's good to just spell it out clearly. The order in this list matters, the earlier items override those that come later.

1. [Breakerbox](https://github.com/yammer/breakerbox)
2. Local service configuration YAML
3. [Defaults](https://github.com/yammer/tenacity#defaults)


Configuration Equations
---------

How to configure your dependent services can be confusing. A good place to start if don't have a predefined SLA is to just look
at actual measurements. At Yammer, we set our max operational time for our actions somewhere between p99 and p999 for response times. We
do this because we have found it to actually be faster to fail those requests, retry, and optimistically get a p50 response time.

1. Tenacity
  -   executionIsolationThreadTimeoutInMillis = `p99 + median + extra`
  -   p99 < executionIsolationThreadTimeoutInMillis < p999
  -   threadpool
      *   size = `(p99 in seconds) * (m1 rate req/sec) + extra`
      *   10 is usually fine for most operations. Anything with a large pool should be understood why that is necessary (e.g. long response times)
      *   Extra: this number only meets the current traffic needs. Make sure to add some extra for bursts as well as growth.

2. HTTP client
  -   connectTimeout = `33% of executionIsolationThreadTimeoutInMillis`
  -   timeout (readTimeout) = `110% of executionIsolationThreadTimeoutInMillis`
      *   We put this timeout higher so that it doesn't raise a TimeoutException from the HTTP client, but instead from Hystrix.

*Note: These are just suggestions, feel free to look at Hystrix's configuration [documentation](https://github.com/Netflix/Hystrix/wiki/Configuration), or implement your own.*

Resources
=========

Tenacity adds resources under `/tenacity`:

1. `GET /tenacity/configuration/propertykeys`:  List of strings which are all the registered propertykeys with Tenacity.
2. `GET /tenacity/configuration/{key}`:         JSON representation of a `TenacityConfiguration` for the supplied {key}.
3. `GET /tenacity/circuitbreakers`:             Simple JSON representation of all circuitbreakers and their circuitbreaker status.
4. `GET /tenacity/metrics.stream`:              text/event-stream of Hystrix metrics.

TenacityExceptionMapper
=======================

An exception mapper exists to serve as an aid for unhandled `HystrixRuntimeException`s. It is used to convert all the types of unhandled exceptions to be converted to a simple
HTTP status code. A common pattern here is to convert the unhandled `HystrixRuntimeException`s to [429 Too Many Requests](http://tools.ietf.org/html/rfc6585#section-4):

```java
TenacityBundleBuilder
                .newBuilder()
                .propertyKeyFactory(propertyKeyFactory)
                .propertyKeys(propertyKeys)
                .mapAllHystrixRuntimeExceptionsTo(429)
                .build();
```

ExceptionLoggingCommandHook
===========================

If you don't handle logging exceptions explicatly within each `TenacityCommand`, you can easily miss problems or at-least find them very hard to debug.
Instead you can add the `ExceptionLoggingCommandHook` to the `TenacityBundle` and register `ExceptionLogger`s to handle the logging of different kinds of Exceptions.
The `ExecutionLoggingCommandHook` acts as a `HystrixCommandExecutionHook` and intercepts all Exceptions that occur during the `run()` method of your `TenacityCommand`s.
By sequencing `ExceptionLogger`s from most specific to most general, the `ExceptionLoggingCommandHook` will be able to find the best `ExceptionLogger` for the type of Exception.

```java
TenacityBundleBuilder
                .newBuilder()
                .propertyKeyFactory(propertyKeyFactory)
                .propertyKeys(propertyKeys)
                .mapAllHystrixRuntimeExceptionsTo(429)
                .commandExecutionHook(new ExceptionLoggingCommandHook(
                    new DBIExceptionLogger(registry),
                    new SQLExceptionLogger(registry),
                    new DefaultExceptionLogger()
                ))
                .build();
```
