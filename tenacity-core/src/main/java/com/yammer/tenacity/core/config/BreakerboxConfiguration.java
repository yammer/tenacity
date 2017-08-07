package com.yammer.tenacity.core.config;

import io.dropwizard.util.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BreakerboxConfiguration {
    @NotNull @Valid
    private String urls = "";

    @NotNull @Valid
    private Duration initialDelay = Duration.seconds(0);

    @NotNull @Valid
    private Duration delay = Duration.seconds(60);

    @NotNull @Valid
    private Duration waitForInitialLoad = Duration.milliseconds(0);

    public BreakerboxConfiguration() { /* Jackson */ }

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public Duration getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(Duration initialDelay) {
        this.initialDelay = initialDelay;
    }

    public Duration getDelay() {
        return delay;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    public Duration getWaitForInitialLoad() {
        return waitForInitialLoad;
    }

    public void setWaitForInitialLoad(Duration waitForInitialLoad) {
        this.waitForInitialLoad = waitForInitialLoad;
    }

    public boolean isWaitForInitialLoad() {
        return waitForInitialLoad.getQuantity() > 0;
    }
}
