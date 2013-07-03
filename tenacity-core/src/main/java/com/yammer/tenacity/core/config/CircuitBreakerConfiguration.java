package com.yammer.tenacity.core.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class CircuitBreakerConfiguration {
    @Min(value = 0)
    @Max(Integer.MAX_VALUE)
    private int requestVolumeThreshold = 20;

    @Min(value = 0)
    @Max(Integer.MAX_VALUE)
    private int sleepWindowInMillis = 5000;

    @Min(value = 0)
    @Max(Integer.MAX_VALUE)
    private int errorThresholdPercentage = 50;

    public CircuitBreakerConfiguration() {}

    public CircuitBreakerConfiguration(int requestVolumeThreshold,
                                       int sleepWindowInMillis,
                                       int errorThresholdPercentage) {
        this.requestVolumeThreshold = requestVolumeThreshold;
        this.sleepWindowInMillis = sleepWindowInMillis;
        this.errorThresholdPercentage = errorThresholdPercentage;
    }

    public int getRequestVolumeThreshold() {
        return requestVolumeThreshold;
    }

    public int getSleepWindowInMillis() {
        return sleepWindowInMillis;
    }

    public int getErrorThresholdPercentage() {
        return errorThresholdPercentage;
    }
}
