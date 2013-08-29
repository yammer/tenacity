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

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int metricsRollingStatisticalWindowInMilliseconds = 10000;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int metricsRollingStatisticalWindowBuckets = 10;

    public CircuitBreakerConfiguration() { /* Jackson */ }


    public CircuitBreakerConfiguration(int requestVolumeThreshold,
                                       int sleepWindowInMillis,
                                       int errorThresholdPercentage,
                                       int metricsRollingStatisticalWindowInMilliseconds,
                                       int metricsRollingStatisticalWindowBuckets) {
        this.requestVolumeThreshold = requestVolumeThreshold;
        this.sleepWindowInMillis = sleepWindowInMillis;
        this.errorThresholdPercentage = errorThresholdPercentage;
        this.metricsRollingStatisticalWindowInMilliseconds = metricsRollingStatisticalWindowInMilliseconds;
        this.metricsRollingStatisticalWindowBuckets = metricsRollingStatisticalWindowBuckets;
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

    public int getMetricsRollingStatisticalWindowInMilliseconds() {
        return metricsRollingStatisticalWindowInMilliseconds;
    }

    public int getMetricsRollingStatisticalWindowBuckets() {
        return metricsRollingStatisticalWindowBuckets;
    }

    public void setRequestVolumeThreshold(int requestVolumeThreshold) {
        this.requestVolumeThreshold = requestVolumeThreshold;
    }

    public void setSleepWindowInMillis(int sleepWindowInMillis) {
        this.sleepWindowInMillis = sleepWindowInMillis;
    }

    public void setErrorThresholdPercentage(int errorThresholdPercentage) {
        this.errorThresholdPercentage = errorThresholdPercentage;
    }

    public void setMetricsRollingStatisticalWindowInMilliseconds(int metricsRollingStatisticalWindowInMilliseconds) {
        this.metricsRollingStatisticalWindowInMilliseconds = metricsRollingStatisticalWindowInMilliseconds;
    }

    public void setMetricsRollingStatisticalWindowBuckets(int metricsRollingStatisticalWindowBuckets) {
        this.metricsRollingStatisticalWindowBuckets = metricsRollingStatisticalWindowBuckets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CircuitBreakerConfiguration that = (CircuitBreakerConfiguration) o;

        if (errorThresholdPercentage != that.errorThresholdPercentage) return false;
        if (metricsRollingStatisticalWindowBuckets != that.metricsRollingStatisticalWindowBuckets) return false;
        if (metricsRollingStatisticalWindowInMilliseconds != that.metricsRollingStatisticalWindowInMilliseconds)
            return false;
        if (requestVolumeThreshold != that.requestVolumeThreshold) return false;
        if (sleepWindowInMillis != that.sleepWindowInMillis) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = requestVolumeThreshold;
        result = 31 * result + sleepWindowInMillis;
        result = 31 * result + errorThresholdPercentage;
        result = 31 * result + metricsRollingStatisticalWindowInMilliseconds;
        result = 31 * result + metricsRollingStatisticalWindowBuckets;
        return result;
    }
}
