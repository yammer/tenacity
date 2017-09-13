package com.yammer.tenacity.core;

import com.netflix.hystrix.*;
import com.netflix.hystrix.metric.consumer.CumulativeCommandEventCounterStream;
import com.netflix.hystrix.metric.consumer.RollingCommandEventCounterStream;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import rx.Observable;

import java.util.function.Supplier;

public abstract class TenacityObservableCommand<R> extends HystrixObservableCommand<R> {
    protected TenacityObservableCommand(TenacityPropertyKey tenacityPropertyKey) {
        super(HystrixObservableCommand.Setter.withGroupKey(TenacityCommand.tenacityGroupKey())
                .andCommandKey(tenacityPropertyKey));
    }

    public static HystrixCommandGroupKey commandGroupKeyFrom(String key) {
        return HystrixCommandGroupKey.Factory.asKey(key);
    }

    public static HystrixCommandProperties getCommandProperties(TenacityPropertyKey key) {
        return HystrixPropertiesFactory.getCommandProperties(key, null);
    }

    public HystrixCommandProperties getCommandProperties() {
        return HystrixPropertiesFactory.getCommandProperties(getCommandKey(), null);
    }

    public static HystrixThreadPoolProperties getThreadpoolProperties(TenacityPropertyKey key) {
        return HystrixPropertiesFactory.getThreadPoolProperties(key, null);
    }

    public static <R> TenacityObservableCommand.Builder<R> builder(TenacityPropertyKey tenacityPropertyKey) {
        return new TenacityObservableCommand.Builder<>(tenacityPropertyKey);
    }

    public HystrixThreadPoolProperties getThreadpoolProperties() {
        return HystrixPropertiesFactory.getThreadPoolProperties(getThreadPoolKey(), null);
    }

    public HystrixCommandMetrics getCommandMetrics() {
        return HystrixCommandMetrics.getInstance(getCommandKey());
    }

    public static HystrixCommandMetrics getCommandMetrics(TenacityPropertyKey key) {
        return HystrixCommandMetrics.getInstance(key);
    }

    public HystrixThreadPoolMetrics getThreadpoolMetrics() {
        return HystrixThreadPoolMetrics.getInstance(getThreadPoolKey());
    }

    public static HystrixThreadPoolMetrics getThreadpoolMetrics(TenacityPropertyKey key) {
        return HystrixThreadPoolMetrics.getInstance(key);
    }

    public HystrixCircuitBreaker getCircuitBreaker() {
        return HystrixCircuitBreaker.Factory.getInstance(getCommandKey());
    }

    public static HystrixCircuitBreaker getCircuitBreaker(TenacityPropertyKey key) {
        return HystrixCircuitBreaker.Factory.getInstance(key);
    }

    public CumulativeCommandEventCounterStream getCumulativeCommandEventCounterStream() {
        return CumulativeCommandEventCounterStream.getInstance(getCommandKey(), getCommandProperties());
    }

    public RollingCommandEventCounterStream getRollingCommandEventCounterStream() {
        return RollingCommandEventCounterStream.getInstance(getCommandKey(), getCommandProperties());
    }

    public static class Builder<R> {
        protected final TenacityPropertyKey key;
        protected Supplier<Observable<R>> run;
        protected Supplier<Observable<R>> fallback;

        public Builder(TenacityPropertyKey key) {
            this.key = key;
        }

        public Builder<R> run(Supplier<Observable<R>> fun) {
            run = fun;
            return this;
        }

        public Builder<R> fallback(Supplier<Observable<R>> fun) {
            fallback = fun;
            return this;
        }

        public TenacityObservableCommand<R> build() {
            if (run == null) {
                throw new IllegalStateException("Run must be supplied.");
            }
            if (fallback == null) {
                return new TenacityObservableCommand<R>(key) {
                    @Override
                    protected Observable<R> construct() {
                        return run.get();
                    }
                };
            } else {
                return new TenacityObservableCommand<R>(key) {
                    @Override
                    protected Observable<R> construct() {
                        return run.get();
                    }

                    @Override
                    protected Observable<R> resumeWithFallback() {
                        return fallback.get();
                    }
                };
            }
        }

        public Observable<R> observe() {
            return build().observe();
        }

        public Observable<R> lazyObservable() {
            return build().toObservable();
        }
    }

    @Override
    protected abstract Observable<R> construct();
}