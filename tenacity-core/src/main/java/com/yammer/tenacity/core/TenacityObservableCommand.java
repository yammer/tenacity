package com.yammer.tenacity.core;

import com.netflix.hystrix.HystrixObservableCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import rx.Observable;

public abstract class TenacityObservableCommand<ReturnType> extends HystrixObservableCommand<ReturnType> {
    public TenacityObservableCommand(TenacityPropertyKey tenacityPropertyKey) {
        super(HystrixObservableCommand.Setter.withGroupKey(TenacityCommand.commandGroupKeyFrom("TENACITY"))
                .andCommandKey(tenacityPropertyKey));
    }

    @Override
    protected abstract Observable<ReturnType> construct();
}