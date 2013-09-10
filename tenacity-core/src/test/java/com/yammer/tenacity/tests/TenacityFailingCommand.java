package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public class TenacityFailingCommand extends TenacityCommand<String> {
    public TenacityFailingCommand(TenacityPropertyKey tenacityPropertyKey) {
        super(tenacityPropertyKey);
    }

    public TenacityFailingCommand(){
        super(DependencyKey.EXAMPLE);
    }

    @Override
    protected String run() throws Exception {
        throw new RuntimeException("purposely failing");
    }

    @Override
    protected String getFallback() {
        return "fallback";
    }
}