package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.TenacityPropertyStore;

public class TenacityFailingCommand extends TenacityCommand<String> {
    public TenacityFailingCommand(TenacityPropertyStore tenacityPropertyStore) {
        super("Test", "Failing", tenacityPropertyStore, DependencyKey.EXAMPLE);
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