package com.yammer.tenacity.core.core;

import com.netflix.hystrix.Hystrix;
import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.TimeUnit;

public class ManagedHystrix implements Managed {
    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        Hystrix.reset(5, TimeUnit.SECONDS);
    }
}