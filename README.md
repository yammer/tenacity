Tenacity
========

What is it?
-----------

Tenacity is a Dropwizard module that incorporates Hystrix. Hystrix is a resiliency library from Netflix. It's goals are to:

1. Stop cascading failures.
2. Fail-fast and rapidly recover.
3. Reduce mean-time-to-discovery (with dashboards)
4. Reduce mean-time-to-recovery (with dynamic configuration)

Modules
-------

-   `tenacity-core`:        The building blocks to quickly use Hystrix within the context of Dropwizard.
-   `tenacity-dashboard`:       Adds the Hystrix dashboard accessiable at `/tenacity`. *Warning: this disables gzip encoding to support text/event-streams*
-   `tenacity-service`:       Utilizes the tenacity-dashboard to view different configurable clusters. Currently this is hosted at http://tenacity.int.yammer.com
-   `tenacity-*-legacy`:        Support for legacy versions of Dropwizard. Currently there is a conflict with <0.6.0 versions (around Bundles).
    Use these instead of their non-legacy counterparts.

How To Use
==========



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

