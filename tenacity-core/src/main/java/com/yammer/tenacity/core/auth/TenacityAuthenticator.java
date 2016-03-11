package com.yammer.tenacity.core.auth;

import com.google.common.base.Optional;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.security.Principal;

public class TenacityAuthenticator<C, P extends Principal> implements Authenticator<C, P> {
    private final Authenticator<C, P> underlying;
    private final TenacityPropertyKey tenacityPropertyKey;

    private TenacityAuthenticator(Authenticator<C, P> underlying, TenacityPropertyKey tenacityPropertyKey) {
        this.underlying = underlying;
        this.tenacityPropertyKey = tenacityPropertyKey;
    }

    public static <C, P extends Principal> Authenticator<C, P> wrap(Authenticator<C, P> authenticator,
            TenacityPropertyKey tenacityPropertyKey) {
        return new TenacityAuthenticator<>(authenticator, tenacityPropertyKey);
    }

    @Override
    public Optional<P> authenticate(C credentials) throws AuthenticationException {
        return new TenacityAuthenticate(credentials).execute();
    }

    private class TenacityAuthenticate extends TenacityCommand<Optional<P>> {
        private final C credentials;

        private TenacityAuthenticate(C credentials) {
            super(tenacityPropertyKey);
            this.credentials = credentials;
        }

        @Override
        protected Optional<P> run() throws Exception {
            return underlying.authenticate(credentials);
        }
    }
}