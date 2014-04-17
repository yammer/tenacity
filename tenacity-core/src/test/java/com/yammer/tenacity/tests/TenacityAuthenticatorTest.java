package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import com.yammer.tenacity.core.auth.TenacityAuthenticator;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.ArchaiusPropertyRegister;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TenacityAuthenticatorTest {
    @Test(expected = HystrixRuntimeException.class)
    public void shouldThrowWhenAuthenticateTimesOut() throws AuthenticationException {
        @SuppressWarnings("unchecked")
        final Authenticator<Object, Object> mockAuthenticator = mock(Authenticator.class);
        final Authenticator<Object, Object> tenacityAuthenticator = TenacityAuthenticator.wrap(
                mockAuthenticator, DependencyKey.TENACITY_AUTH_TIMEOUT);

        final TenacityConfiguration overrideConfiguration = new TenacityConfiguration();
        overrideConfiguration.setExecutionIsolationThreadTimeoutInMillis(1);

        new TenacityPropertyRegister(
                ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(DependencyKey.TENACITY_AUTH_TIMEOUT, overrideConfiguration),
                new BreakerboxConfiguration(),
                mock(ArchaiusPropertyRegister.class))
            .register();

        when(mockAuthenticator.authenticate(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(50);
                return new Object();
            }
        });

        try {
            assertThat(tenacityAuthenticator.authenticate("credentials"))
                .isEqualTo(Optional.absent());
        } catch (HystrixRuntimeException err) {
            assertThat(err.getFailureType()).isEqualTo(HystrixRuntimeException.FailureType.TIMEOUT);
            throw err;
        }
    }
}
