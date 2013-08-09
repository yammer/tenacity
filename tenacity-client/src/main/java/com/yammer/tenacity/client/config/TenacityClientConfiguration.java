package com.yammer.tenacity.client.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class TenacityClientConfiguration {
    @NotNull @Valid
    private final URI uri;

    @JsonCreator

    public TenacityClientConfiguration(@JsonProperty("uri") URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenacityClientConfiguration that = (TenacityClientConfiguration) o;

        if (!uri.equals(that.uri)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
