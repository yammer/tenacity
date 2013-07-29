package com.yammer.tenacity.core.dropwizard;

import com.google.common.base.Optional;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.TenacityPropertyStore;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public abstract class TenacityAuthenticatorOperation<Payload,Response> extends TenacityCommand<Optional<Response>>{

    protected final Authenticator<Payload, Response> authenticator;
    protected final Payload payload;

    public TenacityAuthenticatorOperation(Authenticator<Payload,Response> authenticator, Payload payload, String commandGroupKey, String commandKey,TenacityPropertyStore tenacityPropertyStore, TenacityPropertyKey tenacityPropertyKey) {
        super(commandGroupKey, commandKey, tenacityPropertyStore, tenacityPropertyKey);
        this.authenticator = authenticator;
        this.payload = payload;
    }

    @Override
    protected Optional<Response> run() throws Exception {
        return authenticator.authenticate(payload);
    }
}
