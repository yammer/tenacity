package com.yammer.tenacity.core.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.core.CircuitBreakers;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.logback.shaded.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

public class TenacityCircuitBreakerHealthCheck extends HealthCheck {
    protected Collection<TenacityPropertyKey> propertyKeys;

    public TenacityCircuitBreakerHealthCheck(TenacityPropertyKey... propertyKeys) {
        this(ImmutableList.copyOf(propertyKeys));
    }

    public TenacityCircuitBreakerHealthCheck(Iterable<TenacityPropertyKey> propertyKeys) {
        this.propertyKeys = ImmutableList.copyOf(propertyKeys);
    }

    public String getName() {
        return "tenacity-circuitbreakers";
    }

    @Override
    protected Result check() throws Exception {
        final Collection<CircuitBreaker> openCircuits = CircuitBreakers.allOpen(propertyKeys);

        if (Iterables.isEmpty(openCircuits)) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Open circuit(s): " + Joiner.on(',')
                    .join(Collections2.transform(openCircuits, new Function<CircuitBreaker, String>() {
                        @Nullable
                        @Override
                        public String apply(CircuitBreaker input) {
                            return input.getId().name();
                        }
                    })));
        }
    }
}