package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.TenacityPropertyStore;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public class TenacitySuccessCommand extends TenacityCommand<String> {
    public TenacitySuccessCommand(TenacityPropertyStore tenacityPropertyStore) {
        super("Test", "Success", tenacityPropertyStore, DependencyKey.EXAMPLE);
    }

    public TenacitySuccessCommand(TenacityPropertyStore tenacityPropertyStore,
                                  TenacityPropertyKey tenacityPropertyKey) {
        super("Test", "Override", tenacityPropertyStore, tenacityPropertyKey);
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