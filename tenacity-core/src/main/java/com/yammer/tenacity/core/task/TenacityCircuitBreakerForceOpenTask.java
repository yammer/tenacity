package com.yammer.tenacity.core.task;

import com.google.common.collect.ImmutableMultimap;
import com.netflix.config.ConfigurationManager;
import io.dropwizard.servlets.tasks.Task;

import java.io.PrintWriter;
import java.util.List;

public class TenacityCircuitBreakerForceOpenTask extends Task {
    public TenacityCircuitBreakerForceOpenTask() {
        super("circuitbreakers");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        List<String> key = parameters.get("key").asList();
        List<String> forceOpen = parameters.get("forceOpen").asList();
        if( !key.isEmpty() && !forceOpen.isEmpty() ) {
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command."+key.get(0)+".circuitBreaker.forceOpen", Boolean.valueOf(forceOpen.get(0)) );
        } else {
            throw new IllegalArgumentException("key or forceOpen is missing" );
        }
    }
}
