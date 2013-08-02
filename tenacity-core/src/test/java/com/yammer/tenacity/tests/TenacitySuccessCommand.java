package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public class TenacitySuccessCommand extends TenacityCommand<String> {
    public TenacitySuccessCommand(TenacityPropertyKey tenacityPropertyKey) {
        super(tenacityPropertyKey);
    }

    public TenacitySuccessCommand() {
        this(DependencyKey.EXAMPLE);
    }

    @Override
    protected String run() throws Exception {
        return "value";
    }

    @Override
    protected String getFallback() {
        return "fallback";
    }
}