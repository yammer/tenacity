Tenacity
========

What is it?
-----------

Tenacity is [Dropwizard](http://www.dropwizard.io)+[Hystrix](https://github.com/Netflix/Hystrix).

Dropwizard is a framework for building REST services. Hystrix is a resiliency library from Netflix.

Hystrix goals are to:

1. Stop cascading failures.
2. Fail-fast and rapidly recover.
3. Reduce mean-time-to-discovery (with dashboards)
4. Reduce mean-time-to-recovery (with dynamic configuration)

Tenacity makes Hystrix dropwizard-friendly and for dropwizard-developers to quickly leverage the benefits of Hystrix.

1. Uses dropwizard-bundles for bootstrapping: property strategies, metrics, dynamic configuration, and some resource endpoints (e.g. for dashboards).
2. Dropwizard-configuration style (YAML) for dependencies.
3. Abstractions to clearly configure a dependency operation (`TenacityCommand<ReturnType>`).
4. Ability to unit-test Hystrix: Resets static state held by Hystrix (metrics, counters, etc.). Increases rate at which a concurrent thread updates metrics.
5. Publishes measurements via [Metrics](https://github.com/codahale/metrics).

Modules
-------

-   `tenacity-core`:            The building blocks to quickly use Hystrix within the context of Dropwizard.
-   `tenacity-core-legacy`:     Support for legacy versions of Dropwizard. Currently there is a conflict with <0.6.0 versions (around Bundles).
-   `tenacity-client`:          Client for consuming the resources that `tenacity-core` adds.
-   `tenacity-testing`:         `TenacityTest` allows for easier unit testing. Resets internal state of Hystrix.

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


How to add to your Dropwizard Service
----------

1. To leverage within dropwizard first at the following to your `pom.xml`:

        <dependency>
            <groupId>com.yammer.tenacity</groupId>
            <artifactId>tenacity-core</artifactId>
            <version>0.1.13</version>
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
    bootstrap.addBundle(new TenacityBundle(new CompletieDependencyKeyFactory(), CompletieDependencyKeys.values()));
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
        final ImmutableMap.Builder<TenacityPropertyKey, TenacityConfiguration> builder = ImmutableMap.builder();

        builder.put(CompletieDependencyKey.CMPLT_PRNK_USER, configuration.getRanking().getHystrixUserConfig());
        builder.put(CompletieDependencyKey.CMPLT_PRNK_GROUP, configuration.getRanking().getHystrixGroupConfig());
        builder.put(CompletieDependencyKey.CMPLT_PRNK_SCND_ORDER, configuration.getRanking().getHystrixSecondOrderConfig());
        builder.put(CompletieDependencyKey.CMPLT_PRNK_NETWORK, configuration.getRanking().getHystrixNetworkConfig());
        builder.put(CompletieDependencyKey.CMPLT_TOKIE_AUTH, configuration.getAuthentication().getHystrixConfig());
        builder.put(CompletieDependencyKey.CMPLT_WHVLL_PRESENCE, configuration.getPresence().getHystrixConfig());

        new TenacityPropertyRegister(builder.build(), configuration.getBreakerboxConfiguration()).register();
    }
}
```

4. Use `TenacityCommand` to select which custom tenacity configuration you want to use.

```java
public class CompletieDependencyOnTokie extends TenacityCommand<String> {
    public CompletieDependencyOnTokie() {
        super(CompletieDependencyKey.CMPLT_TOKIE_AUTH);
    }
    ...
}
```

5. When testing use the `tenacity-testing` module. This registers appropriate custom publishers/strategies, clears global `Archaius` configuration state (Hystrix uses internally to manage configuration),
and tweaks threads that calculate metrics which influence circuit breakers to update a more frequent interval. Simply extend the `TenacityTest` helper.

        <dependency>
            <groupId>com.yammer.tenacity</groupId>
            <artifactId>tenacity-testing</artifactId>
            <version>0.1.13</version>
            <scope>test</scope>
        </dependency>

6. Last is to actually configure your dependencies once they are wrapped with `TenacityCommand`.


Configuration
=============

Once you have identified your dependencies you need to configure them appropriately. Here is the basic structure of a single
`TenacityConfiguration` that may be leverage multiple times through your service configuration:

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
One of the great things about Tenacity is the ability to aid in the reduction of mean-time-to-discovery and mean-time-to-recovery for issues. These are available at:

https://breakerbox.int.yammer.com

Breakerbox is a central dashboard and an on-the-fly configuration tool for Tenacity. In addition to the per-tenacity-command configurations shown above this configuration piece let's you define where and how often
to check for newer configurations.

      breakerbox:
          urls: http://breakerbox.sjc1.yammer.com:8080/archaius/{service}
          initialDelay: 10s
          delay: 60s 

-   `urls` is a list of comma-deliminated list of urls for where to pull tenacity configurations. At the moment there are two recommended choices:
    1. `breakerbox.sjc1.yammer.com:8080` for services that are in the `sjc1` data-center.
    2. `breakerbox.bn1.yammer.com:8080` for services that are in the `bn1` data-center.

    Both of these internal VIPs will failover to the other in the event that all backends are unavailable. In other words, if all breakerboxes behind `breakerbox.sjc1.yammer.com` are unavailable then you'll be
    redirected to `breakerbox.bn1.yammer.com`.

-   `initialDelay` how long before the first poll for newer configuration executes.
-   `delay` the ongoing schedule to poll for newer configurations.


Equations
---------

When any percentile data is needed this should be calculated using historical data. Look somewhere between a week to a month
and take the `max` of a particular metric. If the client experiences bursty traffice (such as during a deploy or dependency's
deploy) you should consider this or ensure the clients dependent on that call have sufficient retry logic to sustain failures
on this command.


1. Tenacity
  -   executionIsolationThreadTimeoutInMillis = `ceil(2.0 * (max(p99) + max(median)))`
  -   p99 < executionIsolationThreadTimeoutInMillis < p999
  -   threadpool
      *   size = `(p99 in seconds) * (m1 rate req/sec)`
      *   4 < size <= 20; anything over 20 should be discussed
      *   Note: this number only meets the current traffic needs. Increase this value by some percentage to account for growth.

2. HTTP client
  -   Where `executionIsolationThreadTimeoutInMillis` is the max value of `executionIsolationThreadTimeoutInMillis` over the relevant calls:
      *   Usually this is all the calls to a dependent service, unless there are multiple HTTP clients on the service level
  -   connectTimeout = `33% of executionIsolationThreadTimeoutInMillis`
  -   timeout (readTimeout) = `300% of executionIsolationThreadTimeoutInMillis`
      *   Integrating tenacity offers a greater deal of control and feedback around failures and latent calls; setting the read timeout to the tenacity timeout ensures resource cleanup.

JerseyClient
------------
Our JerseyClient read timeouts have typically been quite generous. For tenacity to function correctly, you should bound your jersey client read
timeouts to be no less than ~20ms higher than the timout of the highest command associcated with that client.

For example,
JerseyClient `J` is used with Commands 
  -  `C1` (executionIsolationThreadTimeoutInMillis=103ms), and
  -  `C2` (executionIsolationThreadTimeoutInMillis=150ms).
  -  Both `C1` and `C2` utilize `J` as a part of their default behavior.

The read timeout for `J` should be set to at least ~170ms. The calls to this client are bounded by the 150ms timeout of `C2`,
but if we simply set `J`s read timeout to be the same, we end up with a non-deterministic result if the timeout occurrs. Sometimes
the jersey client will throw a timeout exception (looking like an error to Tenacity and subsequently Breakerbox), other times Tenacity
will timeout (and propogate that information).

Hystrix Documentation
=====================

https://github.com/Netflix/Hystrix/wiki
