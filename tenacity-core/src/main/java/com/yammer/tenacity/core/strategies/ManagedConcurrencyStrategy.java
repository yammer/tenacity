package com.yammer.tenacity.core.strategies;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.lifecycle.ExecutorServiceManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class ManagedConcurrencyStrategy extends HystrixConcurrencyStrategy {
    private final Environment environment;

    public ManagedConcurrencyStrategy(Environment environment) {
        this.environment = checkNotNull(environment);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
                                            HystrixProperty<Integer> corePoolSize,
                                            HystrixProperty<Integer> maximumPoolSize,
                                            HystrixProperty<Integer> keepAliveTime,
                                            TimeUnit unit,
                                            BlockingQueue<Runnable> workQueue) {
        final String nameFormat = Joiner.on('-').join(ImmutableList.of("hystrix"), threadPoolKey.name(), "-%d");
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(nameFormat)
                .build();
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize.get(),
                maximumPoolSize.get(),
                keepAliveTime.get(),
                unit,
                workQueue,
                threadFactory);
        environment.manage(new ExecutorServiceManager(threadPoolExecutor, 5, TimeUnit.SECONDS, nameFormat));
        return threadPoolExecutor;
    }
}
