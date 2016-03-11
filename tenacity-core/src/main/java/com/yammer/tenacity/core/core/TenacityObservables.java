package com.yammer.tenacity.core.core;

import com.netflix.hystrix.HystrixObservable;
import rx.Observable;

public class TenacityObservables {
    private TenacityObservables() {}

    public static <R> R execute(Observable<R> primary,
                                                  Observable<R> secondary) {
        return primary
                .onErrorResumeNext(secondary)
                .toBlocking()
                .single();
    }

    public static <R> R execute(HystrixObservable<R> primary,
                                                  HystrixObservable<R> secondary) {
        return execute(primary.observe(), secondary.toObservable());
    }

    public static <R> R execute(Observable<R> primary,
                                                  HystrixObservable<R> secondary) {
        return execute(primary, secondary.toObservable());
    }

    public static <R> R execute(HystrixObservable<R> primary,
                                                  Observable<R> secondary) {
        return execute(primary.observe(), secondary);
    }
}