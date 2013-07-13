package com.yammer.tenacity.tests;

import com.netflix.hystrix.Hystrix;
import org.junit.After;

import java.util.concurrent.TimeUnit;

public abstract class TenacityTest {
    @After
    public void testTeardown() {
        Hystrix.reset(1, TimeUnit.SECONDS);
    }
}