package com.yammer.tenacity.core.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CircuitBreaker {
    @NotNull @Valid
    private final TenacityPropertyKey id;
    private final boolean open;

    @JsonCreator
    public CircuitBreaker(@JsonProperty("id") TenacityPropertyKey id,
                          @JsonProperty("open") boolean open) {
        this.id = id;
        this.open = open;
    }

    public static CircuitBreaker open(TenacityPropertyKey id) {
        return new CircuitBreaker(id, true);
    }

    public static CircuitBreaker closed(TenacityPropertyKey id) {
        return new CircuitBreaker(id, false);
    }

    public TenacityPropertyKey getId() {
        return id;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CircuitBreaker that = (CircuitBreaker) o;

        if (open != that.open) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (open ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CircuitBreaker{" +
                "id=" + id +
                ", open=" + open +
                '}';
    }
}