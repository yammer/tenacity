package com.yammer.tenacity.core.healthchecks;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import static com.google.common.base.Preconditions.checkNotNull;

public class TenacityCircuitBreakersHealthcheck extends HealthCheck {
    final Iterable<TenacityPropertyKey> keys;

    public TenacityCircuitBreakersHealthcheck(Iterable<TenacityPropertyKey> keys) {
        super("tenacity-circuitbreakers");
        this.keys = checkNotNull(keys);
    }

    private static Predicate<TenacityPropertyKey> filterOpenCircuitBreakers() {
        return new Predicate<TenacityPropertyKey>() {
            @Override
            public boolean apply(TenacityPropertyKey input) {
                final HystrixCircuitBreaker circuitBreaker = TenacityCommand.getCircuitBreaker(input);
                return circuitBreaker != null && circuitBreaker.isOpen();
            }
        };
    }

    @Override
    protected Result check() throws Exception {
        final ImmutableList<TenacityPropertyKey> openCircuitBreakers =
                FluentIterable
                .from(keys)
                .filter(filterOpenCircuitBreakers())
                .toList();

        if (openCircuitBreakers.isEmpty()) {
            return Result.healthy();
        } else {
            return Result.unhealthy(openCircuitBreakers.toString());
        }
    }
}