package com.yammer.tenacity.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class BreakerboxConfiguration {
    @NotNull @NotEmpty @Valid
    private final String urls;

    @Min(value = 0)
    private final int initialDelay;

    @Min(value = 0)
    private final int delay;

    @JsonCreator
    public BreakerboxConfiguration(@JsonProperty("urls") String urls,
                                   @JsonProperty("initialDelay") Integer initialDelay,
                                   @JsonProperty("delay") Integer delay) {
        this.urls = urls;
        this.initialDelay = Optional.fromNullable(initialDelay).or(10000);
        this.delay = Optional.fromNullable(delay).or(60000);
    }

    public String getUrls() {
        return urls;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BreakerboxConfiguration that = (BreakerboxConfiguration) o;

        if (delay != that.delay) return false;
        if (initialDelay != that.initialDelay) return false;
        if (!urls.equals(that.urls)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = urls.hashCode();
        result = 31 * result + initialDelay;
        result = 31 * result + delay;
        return result;
    }
}