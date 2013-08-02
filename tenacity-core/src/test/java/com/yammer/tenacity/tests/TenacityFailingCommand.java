package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.TenacityCommand;

public class TenacityFailingCommand extends TenacityCommand<String> {
    public TenacityFailingCommand(){
        super(DependencyKey.EXAMPLE);
    }

    @Override
    protected String run() throws Exception {
        throw new RuntimeException();
    }

    @Override
    protected String getFallback() {
        return "fallback";
    }
}