package com.yammer.tenacity.core.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Joiner;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TenacityCircuitBreakerHealthCheck extends HealthCheck{

    protected List<TenacityPropertyKey> propertyKeys;

    public TenacityCircuitBreakerHealthCheck(TenacityPropertyKey... propertyKeys) {
        this.propertyKeys = Arrays.asList(propertyKeys);
    }

    public String toString() {
        return "circuitbreakers";
    }

    @Override
    protected Result check() throws Exception {
        List<String> openCircuits = new ArrayList<>();
        for( TenacityPropertyKey key : propertyKeys ) {
            HystrixCircuitBreaker breaker = TenacityCommand.getCircuitBreaker(key);
            if( breaker != null && !breaker.allowRequest() ) {
                openCircuits.add(key.name());
            }
        }
        return ( openCircuits.isEmpty() ) ? Result.healthy() : Result.unhealthy("These circuit(s) is opened: " + Joiner.on(',').join(openCircuits));
    }
}
