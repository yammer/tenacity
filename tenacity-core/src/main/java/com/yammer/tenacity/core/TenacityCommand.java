package com.yammer.tenacity.core;

import com.netflix.hystrix.*;
import com.netflix.hystrix.metric.consumer.CumulativeCommandEventCounterStream;
import com.netflix.hystrix.metric.consumer.RollingCommandEventCounterStream;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import rx.Observable;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public abstract class TenacityCommand<R> extends HystrixCommand<R> {
    protected TenacityCommand(TenacityPropertyKey tenacityPropertyKey) {
        this(tenacityPropertyKey, tenacityPropertyKey);
    }

    protected TenacityCommand(TenacityPropertyKey commandKey,
                              TenacityPropertyKey threadpoolKey) {
        super(HystrixCommand.Setter.withGroupKey(tenacityGroupKey())
                .andCommandKey(commandKey)
                .andThreadPoolKey(threadpoolKey));
    }

    static HystrixCommandGroupKey tenacityGroupKey() {
        return commandGroupKeyFrom("TENACITY");
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

    public static <R> Builder<R> builder(TenacityPropertyKey tenacityPropertyKey) {
        return new Builder<>(tenacityPropertyKey);
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
        protected Supplier<R> run;
        protected Supplier<R> fallback;

        public Builder(TenacityPropertyKey key) {
            this.key = key;
        }

        public Builder<R> run(Supplier<R> fun) {
            run = fun;
            return this;
        }

        public Builder<R> fallback(Supplier<R> fun) {
            fallback = fun;
            return this;
        }

        public TenacityCommand<R> build() {
            if (run == null) {
                throw new IllegalStateException("Run must be supplied.");
            }
            if (fallback == null) {
                return new TenacityCommand<R>(key) {
                    @Override
                    protected R run() throws Exception {
                        return run.get();
                    }
                };
            } else {
                return new TenacityCommand<R>(key) {
                    @Override
                    protected R run() throws Exception {
                        return run.get();
                    }

                    @Override
                    protected R getFallback() {
                        return fallback.get();
                    }
                };
            }
        }

        public R execute() {
            return build().execute();
        }

        public Future<R> queue() {
            return build().queue();
        }

        public Observable<R> observe() {
            return build().observe();
        }
    }

    @Override
    protected abstract R run() throws Exception;
}
