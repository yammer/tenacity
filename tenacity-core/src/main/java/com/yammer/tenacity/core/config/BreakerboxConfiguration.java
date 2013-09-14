package com.yammer.tenacity.core.config;

import com.yammer.dropwizard.util.Duration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

public class BreakerboxConfiguration {
    @NotNull @Valid
    private String urls = "";

    @NotNull @Valid
    private Duration initialDelay = Duration.seconds(10);

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BreakerboxConfiguration that = (BreakerboxConfiguration) o;

        if (!delay.equals(that.delay)) return false;
        if (!initialDelay.equals(that.initialDelay)) return false;
        if (!urls.equals(that.urls)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = urls.hashCode();
        result = 31 * result + initialDelay.hashCode();
        result = 31 * result + delay.hashCode();
        return result;
    }
}
