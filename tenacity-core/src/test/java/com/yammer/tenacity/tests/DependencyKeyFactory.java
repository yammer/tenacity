package com.yammer.tenacity.tests;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

public class DependencyKeyFactory implements TenacityPropertyKeyFactory {
    @Override
    public TenacityPropertyKey from(String value) {
        return DependencyKey.valueOf(value.toUpperCase());
    }
}