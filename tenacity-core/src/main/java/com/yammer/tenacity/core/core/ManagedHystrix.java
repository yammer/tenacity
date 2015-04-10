package com.yammer.tenacity.core.core;

import com.netflix.hystrix.Hystrix;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;

public class ManagedHystrix implements Managed {
    protected final Duration shutdownGracePeriod;

    public ManagedHystrix(Duration shutdownGracePeriod) {
        this.shutdownGracePeriod = shutdownGracePeriod;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        Hystrix.reset(shutdownGracePeriod.getQuantity(), shutdownGracePeriod.getUnit());
    }
}