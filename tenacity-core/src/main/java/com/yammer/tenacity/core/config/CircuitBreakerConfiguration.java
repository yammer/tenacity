package com.yammer.tenacity.core.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Objects;

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
    public int hashCode() {
        return Objects.hash(requestVolumeThreshold, sleepWindowInMillis, errorThresholdPercentage, metricsRollingStatisticalWindowInMilliseconds, metricsRollingStatisticalWindowBuckets);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CircuitBreakerConfiguration other = (CircuitBreakerConfiguration) obj;
        return Objects.equals(this.requestVolumeThreshold, other.requestVolumeThreshold)
                && Objects.equals(this.sleepWindowInMillis, other.sleepWindowInMillis)
                && Objects.equals(this.errorThresholdPercentage, other.errorThresholdPercentage)
                && Objects.equals(this.metricsRollingStatisticalWindowInMilliseconds, other.metricsRollingStatisticalWindowInMilliseconds)
                && Objects.equals(this.metricsRollingStatisticalWindowBuckets, other.metricsRollingStatisticalWindowBuckets);
    }

    @Override
    public String toString() {
        return "CircuitBreakerConfiguration{" +
                "requestVolumeThreshold=" + requestVolumeThreshold +
                ", sleepWindowInMillis=" + sleepWindowInMillis +
                ", errorThresholdPercentage=" + errorThresholdPercentage +
                ", metricsRollingStatisticalWindowInMilliseconds=" + metricsRollingStatisticalWindowInMilliseconds +
                ", metricsRollingStatisticalWindowBuckets=" + metricsRollingStatisticalWindowBuckets +
                '}';
    }
}
