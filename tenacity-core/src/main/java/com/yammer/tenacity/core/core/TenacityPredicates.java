package com.yammer.tenacity.core.core;

import com.google.common.base.Predicate;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public class TenacityPredicates {
    private TenacityPredicates() {}

    private static class IsEqualToPredicate implements Predicate<TenacityPropertyKey> {
        private final TenacityPropertyKey isEqualTo;

        public IsEqualToPredicate(TenacityPropertyKey isEqualTo) {
            this.isEqualTo = isEqualTo;
        }

        @Override
        public boolean apply(TenacityPropertyKey input) {
            return input != null && input.name().equals(isEqualTo.name());
        }
    }

    public static Predicate<TenacityPropertyKey> isEqualTo(TenacityPropertyKey key) {
        return new IsEqualToPredicate(key);
    }
}