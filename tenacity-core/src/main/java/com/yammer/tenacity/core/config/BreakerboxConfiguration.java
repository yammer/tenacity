package com.yammer.tenacity.core.config;

import io.dropwizard.util.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class BreakerboxConfiguration {
    @NotNull @Valid
    private String urls = "";

    @NotNull @Valid
    private Duration initialDelay = Duration.seconds(0);

    @NotNull @Valid
    private Duration delay = Duration.seconds(60);

    public BreakerboxConfiguration() { /* Jackson */ }

    public BreakerboxConfiguration(String urls, Duration initialDelay, Duration delay) {
        this.urls = checkNotNull(urls);
        this.initialDelay = checkNotNull(initialDelay);
        this.delay = checkNotNull(delay);
    }

    public String getUrls() {
        return urls;
    }

    public Duration getInitialDelay() {
        return initialDelay;
    }

    public Duration getDelay() {
        return delay;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public void setInitialDelay(Duration initialDelay) {
        this.initialDelay = initialDelay;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(urls, initialDelay, delay);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BreakerboxConfiguration other = (BreakerboxConfiguration) obj;
        return Objects.equals(this.urls, other.urls)
                && Objects.equals(this.initialDelay, other.initialDelay)
                && Objects.equals(this.delay, other.delay);
    }
}
