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

