package com.yammer.tenacity.tests;

import com.google.common.base.Optional;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.tenacity.core.dropwizard.TenacityAuthenticatorOperation;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TenacityAuthenticatorTest extends AbstractTenacityTest {
    public static final class MockAuthenticatorOperation extends TenacityAuthenticatorOperation<String, Boolean> {

        protected MockAuthenticatorOperation(Authenticator<String, Boolean> authenticator, String authString, TenacityPropertyKey tenacityPropertyKey) {
            super(authenticator, authString, tenacityPropertyKey);
        }

        @Override
        protected Optional<Boolean> getFallback() {
            return Optional.of(false);
        }
    }

    public static final class MockAuthenticator implements Authenticator<String, Boolean>{
        private final TenacityPropertyKey propertyKey;

        public MockAuthenticator(TenacityPropertyKey propertyKey) {
            this.propertyKey = propertyKey;
        }

        @Override
        public Optional<Boolean> authenticate(String authString) throws AuthenticationException {
            return new MockAuthenticatorOperation(new Authenticator<String, Boolean>() {
                @Override
                public Optional<Boolean> authenticate(String s) throws AuthenticationException {
                    if(s.equalsIgnoreCase("valid")){
                        return Optional.of(true);
                    }else if(s.equalsIgnoreCase("noauth")){
                        throw new AuthenticationException("Invalid account!");
                    }
                    return Optional.of(false);
                }
            },
                    authString,
                    propertyKey
            ).execute();
        }
    }

    @Test
    public void testRun() throws AuthenticationException {
        MockAuthenticator authenticator = new MockAuthenticator(DependencyKey.EXAMPLE);

        Optional<Boolean> invalidAuth = authenticator.authenticate("INVALID");
        assertTrue(invalidAuth.isPresent());
        assertFalse(invalidAuth.get());

        Optional<Boolean> validAuth = authenticator.authenticate("VALID");
        assertTrue(validAuth.isPresent());
        assertTrue(validAuth.get());
    }

    @Test
    public void testFallback() throws AuthenticationException {
        MockAuthenticator authenticator = new MockAuthenticator(DependencyKey.EXAMPLE);

        Optional<Boolean> noAuth = authenticator.authenticate("noauth");

        assertTrue(noAuth.isPresent());
        assertFalse(noAuth.get());

        final HystrixCommandMetrics authCommandMetrics = HystrixCommandMetrics
                .getInstance(new MockAuthenticatorOperation(null, "MockAuth", DependencyKey.EXAMPLE).getCommandKey());
        assertThat(authCommandMetrics
                .getCumulativeCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS))
                .isEqualTo(1);
    }
}