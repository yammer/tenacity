package com.yammer.tenacity.core.properties;

public class StringTenacityPropertyKeyFactory implements TenacityPropertyKeyFactory {
    @Override
    public TenacityPropertyKey from(final String value) {
        return new TenacityPropertyKey() {
            @Override
            public String name() {
                return value.toUpperCase();
            }

            @Override
            public String toString() {
                return name();
            }
        };
    }
}