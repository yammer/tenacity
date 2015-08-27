package com.yammer.tenacity.core.core;

import com.netflix.hystrix.HystrixObservable;
import rx.Observable;

public class TenacityObservables {
    private TenacityObservables() {}

    public static <ReturnType> ReturnType execute(Observable<ReturnType> primary,
                                                  Observable<ReturnType> secondary) {
        return primary
                .onErrorResumeNext(secondary)
                .toBlocking()
                .single();
    }

    public static <ReturnType> ReturnType execute(HystrixObservable<ReturnType> primary,
                                                  HystrixObservable<ReturnType> secondary) {
        return execute(primary.observe(), secondary.toObservable());
    }

    public static <ReturnType> ReturnType execute(Observable<ReturnType> primary,
                                                  HystrixObservable<ReturnType> secondary) {
        return execute(primary, secondary.toObservable());
    }

    public static <ReturnType> ReturnType execute(HystrixObservable<ReturnType> primary,
                                                  Observable<ReturnType> secondary) {
        return execute(primary.observe(), secondary);
    }
}