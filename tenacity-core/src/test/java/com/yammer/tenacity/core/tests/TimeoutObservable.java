package com.yammer.tenacity.core.tests;

import com.yammer.tenacity.core.TenacityObservableCommand;
import com.yammer.tenacity.tests.DependencyKey;
import io.dropwizard.util.Duration;
import rx.Observable;
import rx.schedulers.Schedulers;

import static org.junit.Assert.fail;

public class TimeoutObservable extends TenacityObservableCommand<Boolean> {
    private final Duration sleepDuration;

    public TimeoutObservable(Duration sleepDuration) {
        super(DependencyKey.OBSERVABLE_TIMEOUT);
        this.sleepDuration = sleepDuration;
    }

    @Override
    protected Observable<Boolean> construct() {
        return Observable.<Boolean>create((subscriber) -> {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onStart();
                try {
                    Thread.sleep(sleepDuration.toMilliseconds());
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                } catch (InterruptedException err) {
                    subscriber.onError(err);
                    fail("Interrupted observe timeout");
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}