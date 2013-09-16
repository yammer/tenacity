Tenacity
========

What is it?
-----------

Tenacity is a Dropwizard module that incorporates Hystrix. Hystrix is a resiliency library from Netflix. Hystrix goals are to:

1. Stop cascading failures.
2. Fail-fast and rapidly recover.
3. Reduce mean-time-to-discovery (with dashboards)
4. Reduce mean-time-to-recovery (with dynamic configuration)

Tenacity aids at making Hystrix dropwizard-friendly and constructing an ecosystem for developers to quickly leverage the benefits of Hystrix.

1. Bundles setup necessary property strategies, metrics, dynamic configuration, and some resource endpoints (e.g. for dashboards).
2. Dropwizard-configuration style (YAML) for dependencies.
3. Building blocks to inject different configuration dependencies into your dependency wrappers (`TenacityCommand<ReturnType>`).
4. Ability to unit test Hystrix. We have tried to mitigate a lot of the problems because Hystrix relies heavily on static state.

Why?
----

When leveraging dependencies it is expected that they will fail eventually. Good practices help mitigate failing dependencies, but they don't completely
solve the issue. For example, we often apply a timeout to dependencies over the network to at least put a simple cancellation mechanism.
Ignoring the fact that the current timeout we use is actually a read-timeout, it in some circumstances actually makes the situation worse.

There are certain scenarios where servers need to shed-load in order to come back to an optimum throughput level. This may be because
of a loss of cache, network partition, etc. Instead what happens is they still receive the same amount of traffic and in some circumstances more
if clients of that service timeout and retry quickly.

The approach we are trying to take with Tenacity is to move forward with a library that will augment good practices for
managing failing dependencies.

Modules
-------

-   `tenacity-core`:        The building blocks to quickly use Hystrix within the context of Dropwizard.
-   `tenacity-dashboard`:       Adds the Hystrix dashboard accessiable at `/tenacity`. *Warning: this disables gzip encoding to support text/event-streams*
-   `tenacity-*-legacy`:        Support for legacy versions of Dropwizard. Currently there is a conflict with <0.6.0 versions (around Bundles).
-   `tenacity-testing`:         `TenacityTest` helper to aid in clearing state between runs for Archaius dynamic configurations. Also tunes the Metric calculations to occur at a faster rate.
    Use these instead of their non-legacy counterparts.

Philosophy
----------

Tenacity in a nutshell protects services from it's dependencies. It does this through a couple of different mechanisms. All dependent commands should be wrapped with
the `TenacityCommand`. This isolates the application code from it's dependencies through the JVM's threadpools and `Future`s. It then forces
the developer to consider how much resources to allocate and to constrain how long dependencies should take for the application to be able to manage failing dependencies.
Tenacity tries to provide you with equations to aid in how to determine how many resources and how long dependencies should take based on data we collect
through `metrics`.

Lastly, it provides circuit-breakers. Circuit-breakers allow for any `TenacityCommand` that is currently experiencing a high number of failures to
short-circuit the invocation of `run()` and instead immediately use the `getFallback()`. This has two benefits. First it allows the
 application to shed load and fail-fast instead of queueing. This is configurable and
 has reasonable defaults. Secondly, if the reason for `run()` taking longer then expected is due to server-side performance degradation
 this allows for the client to reduce it's traffic to the server, assuming it may be part of the problem.

How To Use
==========

Here is a sample `TenacityCommand` that always succeeds:

    public class AlwaysSucceed extends TenacityCommand<String> {
        public AlwaysSucceed() {
            super(DependencyKey.ALWAYS_SUCCEED);
        }

        @Override
        protected String run() throws Exception {
            return "value";
        }

        @Override
        protected String getFallback() {
            return "fallback";
        }
    }

There are two ways to use a constructed `TenacityCommand`.

    AlwaysSucceed command = new AlwaysSucceed();
    String result = command.execute();

This executes the command synchronously but through the protection of a `Future.get(configurableTimeout)`. The other way to invoke
a `TenacityCommand` is asynchronously.

    Future<String> futureResult = command.queue();

There are two methods that must be overriden, `run()` and `getFallback()`. `getFallback()` is *always* invoked. It can be invoked one of four different ways:

1. `run()` throws an exception.
2. `run()` is never executed due to an open circuit.
3. `run()` is never executed due a queue-rejection. This can be because there aren't enough threads or queue space available.
4. `run()` takes too long to return a result. The thread waiting for the result will not receive the result from the invocation of `getFallback()` instead.

![Alt text](https://raw.github.com/wiki/Netflix/Hystrix/images/hystrix-flow-chart-original.png)

Caveats:

1. `getFallback()` should not be a latent or blocking call.

TenacityCommand Constructor Arguments
-------------------------------------

Earlier we saw:

    public class AlwaysSucceed extends TenacityCommand<String> {
        public AlwaysSucceed() {
            super(DependencyKey.ALWAYS_SUCCEED);
        }
        ...
    }

The arguments are:

1. `commandKey`: This creates a circuit-breaker, threadpool, and also the identifier that will be used in dashboards.
This should be your implementation of the `TenacityProperyKey` interface.

It is possible to create multiple circuit-breakers that leverage a single threadpool, but for simplicity we are not allowing that type of configuration.

Recommended Use
---------------

An example service called `A` depends on `B` for some functionality. This may appear as a single dependency, but that may
not necessarily be true. What if `A` made 5 different invocations on `B` to satisfy one requirement that `A`
needed to accomplish in order to meet a functional need. Then another requirement only required 1 invocation. In practice,
these two different uses might have much different throughput and invocation rates. Thus, you should treat these two different
usages of `B` as two separate dependencies of `A`. This allows for finer configuration of the two dependencies, but also helps
isolate resources and circuit-breakers to a well defined scope.

Dropwizard
----------

1. To leverage within dropwizard first at the following to your `pom.xml`:

        <dependency>
            <groupId>com.yammer.tenacity</groupId>
            <artifactId>tenacity-core</artifactId>
            <version>0.1.4</version>
        </dependency>


2. Enumerate your dependencies. These will eventually be used as global identifiers in dashboards. We have found that it works best
when you include the service and the external dependency at a minimum. Here is an example of `completie`'s dependencies. Note we also
shave down some characters to save on space, again for UI purposes. In addition, you'll need to have an implementation of a `TenacityPropertyKeyFactory` which you can see an example of below.

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

3. Then make sure you add the bundle in your `Service` and register your custom tenacity properties. Here we made use of a helper class
to register properties given a `CompletieConfiguration`. This is helpful when you might need to register custom properties from multiple locations
such as application and testing code. Note the helper class makes use the `TenacityPropertyRegister` which needs a much more general: `ImmutableMap.Builder<TenacityPropertyKey, TenacityConfiguration>` type.


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

        ////////////////////////////////////////////////

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

4. Use `TenacityCommand` to select which custom tenacity configuration you want to use.

        public class CompletieDependencyOnTokie extends TenacityCommand<String> {
            public CompletieDependencyOnTokie() {
                super(CompletieDependencyKey.CMPLT_TOKIE_AUTH);
            }
            ...
        }

5. When testing use the `tenacity-testing` module. This registers appropriate custom publishers/strategies, clears global `Archaius` configuration state (Hystrix uses internally to manage configuration),
and tweaks threads that calculate metrics which influence circuit breakers to update a more frequent interval. Simply extend the `TenacityTest` helper.

        <dependency>
            <groupId>com.yammer.tenacity</groupId>
            <artifactId>tenacity-testing</artifactId>
            <version>0.1.1</version>
            <scope>test</scope>
        </dependency>

6. Last is to actually configure your dependencies once they are wrapped with `TenacityCommand`.


Configuration
=============

Once you have identified your dependencies you need to configure them appropriately. Here is the basic structure of a single
`TenacityConfiguration` that may be leverage multiple times through your service configuration:

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

Service Dashboards
==================

One of the great things about Tenacity is the ability to aid in the reduction of mean-time-to-discovery for issues. These are available at:

https://breakerbox.int.yammer.com

Hystrix Documentation
=====================

https://github.com/Netflix/Hystrix/wiki
