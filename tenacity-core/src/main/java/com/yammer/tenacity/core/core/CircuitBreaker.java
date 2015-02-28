package com.yammer.tenacity.core.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

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
    public int hashCode() {
        return Objects.hash(id, open);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CircuitBreaker other = (CircuitBreaker) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.open, other.open);
    }

    @Override
    public String toString() {
        return "CircuitBreaker{" +
                "id=" + id +
                ", open=" + open +
                '}';
    }
}