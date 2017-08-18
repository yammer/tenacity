package com.yammer.tenacity.tests;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.yammer.tenacity.core.TenacityObservableCommand;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TenacityObservableCollapserTest {
    private static class TenacityCollapser extends TenacityObservableCommand<Long> {
        final Collection<HystrixCollapser.CollapsedRequest<Long, Long>> requests;

        public TenacityCollapser(Collection<HystrixCollapser.CollapsedRequest<Long, Long>> collapsedRequests) {
            super(DependencyKey.OBSERVABLE_TIMEOUT);
            this.requests = collapsedRequests;
        }

        @Override
        protected Observable<Long> construct() {
            long response = 0;
            for (HystrixCollapser.CollapsedRequest<Long, Long> request : requests) {
                response += request.getArgument();
            }
            return Observable.just(response);
        }
    }

    private static class Collapser extends HystrixObservableCollapser<Integer, Long, Long, Long> {
        private final long key;

        public Collapser(long key) {
            this.key = key;
        }

        @Override
        public Long getRequestArgument() {
            return key;
        }

        @Override
        protected HystrixObservableCommand<Long> createCommand(Collection<HystrixCollapser.CollapsedRequest<Long, Long>> collapsedRequests) {
            return new TenacityCollapser(collapsedRequests);
        }

        @Override
        protected Func1<Long, Integer> getBatchReturnTypeKeySelector() {
            return Long::intValue;
        }

        @Override
        protected Func1<Long, Integer> getRequestArgumentKeySelector() {
            return Long::intValue;
        }

        @Override
        protected void onMissingResponse(HystrixCollapser.CollapsedRequest<Long, Long> r) {
            r.setResponse(r.getArgument());
        }

        @Override
        protected Func1<Long, Long> getBatchReturnTypeToResponseTypeMapper() {
            return (aLong) -> aLong;
        }
    }

    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void simpleCollapse() throws Exception {
        final HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            final Observable<Long> o1 = new Collapser(1L).observe();
            final Observable<Long> o2 = new Collapser(2L).observe();

            o1.subscribe(new Subscriber<Long>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    fail(e.toString());
                }

                @Override
                public void onNext(Long aLong) {
                    assertThat(aLong).isEqualTo(1L);
                }
            });

            o2.subscribe(new Subscriber<Long>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    fail(e.toString());
                }

                @Override
                public void onNext(Long aLong) {
                    assertThat(aLong).isEqualTo(2L);
                }
            });

            for (HystrixInvokableInfo<?> info : HystrixRequestLog.getCurrentRequest().getAllExecutedCommands()) {
                assertThat(info.isExecutionComplete()).isTrue();
                assertThat(info.isSuccessfulExecution()).isTrue();
                assertThat(info.getExecutionEvents()).contains(HystrixEventType.COLLAPSED, HystrixEventType.SUCCESS);
            }

        } finally {
            context.shutdown();
        }
    }
}