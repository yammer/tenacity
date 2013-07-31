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
-   `tenacity-service`:       Utilizes the tenacity-dashboard to view different configurable clusters. Currently this is hosted at http://tenacity.int.yammer.com
-   `tenacity-*-legacy`:        Support for legacy versions of Dropwizard. Currently there is a conflict with <0.6.0 versions (around Bundles).
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
        public AlwaysSucceed(TenacityPropertyStore tenacityPropertyStore) {
            super("ExampleGroup", tenacityPropertyStore, DependencyKey.ALWAYS_SUCCEED);
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

    AlwaysSucceed command = new AlwaysSucceed(...);
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
        public AlwaysSucceed(TenacityPropertyStore tenacityPropertyStore) {
            super("ExampleGroup", tenacityPropertyStore, DependencyKey.ALWAYS_SUCCEED);
        }
        ...
    }

The arguments are:

1. `commandGroupKey`: This allows for a grouping mechanism for `commandKey`s.
2. `tenacityPropertyStore`: this is used internally to select which configuration to use based off the 3rd argument.
3. `commandKey`: This creates a circuit-breaker, threadpool, and also the identifier that will be used in dashboards.
This should be your implementation of the `TenacityProperyKey` interface.

It is possible to create multiple circuit-breakers that leverage a single threadpool, but for brevity and simplicity that is left out of the documentation.
There is another constructor that allows for separate `commandKey` and `threadpoolKey` for that functionality. We have not found the
need for this case with our use cases with Tenacity so far.

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
            <version>0.0.6</version>
        </dependency>

2. Then make sure you add the bundle in your `Service`.

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            ...
            bootstrap.addBundle(new TenacityBundle());
            ...
        }

3. Enumerate your dependencies. These will eventually be used as global identifiers in dashboards. We have found that it works best
when you include the service and the external dependency at a minimum. Here is an example of `completie`'s dependencies. Note we also
shave down some characters to save on space, again for UI purposes.

            public enum CompletieDependencyKeys implements TenacityPropertyKey {
                CMPLT_PRNK_USER, CMPLT_PRNK_GROUP, CMPLT_PRNK_SCND_ORDER, CMPLT_PRNK_NETWORK,
                CMPLT_TOKIE_AUTH,
                CMPLT_TYRANT_AUTH,
                CMPLT_WHVLL_PRESENCE
            }

4. Create a concrete implementation of the `TenacityPropertyStoreBuilder<? extends Configuration>` as this is used to create
the `TenacityPropertyStore`. The store is then injected into all `TenacityCommand`s to tie together configurations and dependencies.
Here is `completie`'s:

            public class CompletiePropertiesBuilder extends TenacityPropertyStoreBuilder<CompletieConfiguration> {
                public CompletiePropertiesBuilder(CompletieConfiguration configuration) {
                    super(configuration);
                }

                @Override
                public ImmutableMap<TenacityPropertyKey, HystrixCommandProperties.Setter> buildCommandProperties() {
                    final ImmutableMap.Builder<TenacityPropertyKey, TenacityCommandProperties.Setter> builder = ImmutableMap.builder();

                    builder.put(CompletieDependencyKey.CMPLT_PRNK_USER, TenacityCommandProperties.build(configuration.getRanking().getHystrixUserConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_PRNK_GROUP, TenacityCommandProperties.build(configuration.getRanking().getHystrixGroupConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_PRNK_SCND_ORDER, TenacityCommandProperties.build(configuration.getRanking().getHystrixSecondOrderConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_PRNK_NETWORK, TenacityCommandProperties.build(configuration.getRanking().getHystrixNetworkConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_TOKIE_AUTH, TenacityCommandProperties.build(configuration.getAuthentication().getHystrixConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_WHVLL_PRESENCE, TenacityCommandProperties.build(configuration.getPresence().getHystrixConfig()));

                    return builder.build();
                }

                @Override
                public ImmutableMap<TenacityPropertyKey, HystrixThreadPoolProperties.Setter> buildThreadpoolProperties() {
                    final ImmutableMap.Builder<TenacityPropertyKey, TenacityThreadPoolProperties.Setter> builder = ImmutableMap.builder();

                    builder.put(CompletieDependencyKey.CMPLT_PRNK_USER, TenacityThreadPoolProperties.build(configuration.getRanking().getHystrixUserConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_PRNK_GROUP, TenacityThreadPoolProperties.build(configuration.getRanking().getHystrixGroupConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_PRNK_SCND_ORDER, TenacityThreadPoolProperties.build(configuration.getRanking().getHystrixSecondOrderConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_PRNK_NETWORK, TenacityThreadPoolProperties.build(configuration.getRanking().getHystrixNetworkConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_TOKIE_AUTH, TenacityThreadPoolProperties.build(configuration.getAuthentication().getHystrixConfig()));
                    builder.put(CompletieDependencyKey.CMPLT_WHVLL_PRESENCE, TenacityThreadPoolProperties.build(configuration.getPresence().getHystrixConfig()));

                    return builder.build();
                }
            }

5. In your `Service`'s initialization phase construct your `TenacityPropertyStore`. Then inject it to your abstractions which handle functionality
where dependencies are utilized.

            TenacityPropertyStore tenacityPropertyStore = new TenacityPropertyStore(new CompletiePropertiesBuilder(configuration));

            SomeDependency someDependency = new SomeDependency(tenacityPropertyStore, ...);

6. Next is to actually configure your dependencies once they are wrapped with `TenacityCommand`s.
Configuration
=============

When any percentile data is needed this should be calculated using historical data. Look somewhere between a week to a month
and take the `max` of a particular metric.

1. Tenacity
  -   executionIsolationThreadTimeoutInMillis = `ceil(1.1 * (max(p99) + max(median)))`
  -   threadpool
      *   size = `(p99 in seconds) * (m1 rate req/sec)` [minimum of 2, anything over 20 should be discussed]

2. HTTP client
  -   connectTimeout = `33% of executionIsolationThreadTimeoutInMillis`
  -   timeout (readTimeout) = `66% of executionIsolationThreadTimeoutInMillis`
  -   -   there is a good chance that a single client may use multiple commandKeys that might warrant a range of different connectionTimeouts and read timeouts. 
  -   In this case the current approach is to take the max value across all specific timeouts (alternatively the clients could be split into multiple clients on the service level)

