package com.yammer.tenacity.core.config;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Objects;

public class ThreadPoolConfiguration {

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int threadPoolCoreSize = 10;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int keepAliveTimeMinutes = 1;

    @Min(-1)
    @Max(Integer.MAX_VALUE)
    private int maxQueueSize = -1;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int queueSizeRejectionThreshold = 5;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int metricsRollingStatisticalWindowInMilliseconds = 10000;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int metricsRollingStatisticalWindowBuckets = 10;

    public ThreadPoolConfiguration() { /* Jackson */ }


    public ThreadPoolConfiguration(int threadPoolCoreSize,
                                   int keepAliveTimeMinutes,
                                   int maxQueueSize,
                                   int queueSizeRejectionThreshold,
                                   int metricsRollingStatisticalWindowInMilliseconds,
                                   int metricsRollingStatisticalWindowBuckets) {
        this.threadPoolCoreSize = threadPoolCoreSize;
        this.keepAliveTimeMinutes = keepAliveTimeMinutes;
        this.maxQueueSize = maxQueueSize;
        this.queueSizeRejectionThreshold = queueSizeRejectionThreshold;
        this.metricsRollingStatisticalWindowInMilliseconds = metricsRollingStatisticalWindowInMilliseconds;
        this.metricsRollingStatisticalWindowBuckets = metricsRollingStatisticalWindowBuckets;
    }

    public int getThreadPoolCoreSize() {
        return threadPoolCoreSize;
    }

    public int getKeepAliveTimeMinutes() {
        return keepAliveTimeMinutes;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public int getQueueSizeRejectionThreshold() {
        return queueSizeRejectionThreshold;
    }

    public int getMetricsRollingStatisticalWindowInMilliseconds() {
        return metricsRollingStatisticalWindowInMilliseconds;
    }

    public int getMetricsRollingStatisticalWindowBuckets() {
        return metricsRollingStatisticalWindowBuckets;
    }

    public void setThreadPoolCoreSize(int threadPoolCoreSize) {
        this.threadPoolCoreSize = threadPoolCoreSize;
    }

    public void setKeepAliveTimeMinutes(int keepAliveTimeMinutes) {
        this.keepAliveTimeMinutes = keepAliveTimeMinutes;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public void setQueueSizeRejectionThreshold(int queueSizeRejectionThreshold) {
        this.queueSizeRejectionThreshold = queueSizeRejectionThreshold;
    }

    public void setMetricsRollingStatisticalWindowInMilliseconds(int metricsRollingStatisticalWindowInMilliseconds) {
        this.metricsRollingStatisticalWindowInMilliseconds = metricsRollingStatisticalWindowInMilliseconds;
    }

    public void setMetricsRollingStatisticalWindowBuckets(int metricsRollingStatisticalWindowBuckets) {
        this.metricsRollingStatisticalWindowBuckets = metricsRollingStatisticalWindowBuckets;
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadPoolCoreSize, keepAliveTimeMinutes, maxQueueSize, queueSizeRejectionThreshold, metricsRollingStatisticalWindowInMilliseconds, metricsRollingStatisticalWindowBuckets);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ThreadPoolConfiguration other = (ThreadPoolConfiguration) obj;
        return Objects.equals(this.threadPoolCoreSize, other.threadPoolCoreSize)
                && Objects.equals(this.keepAliveTimeMinutes, other.keepAliveTimeMinutes)
                && Objects.equals(this.maxQueueSize, other.maxQueueSize)
                && Objects.equals(this.queueSizeRejectionThreshold, other.queueSizeRejectionThreshold)
                && Objects.equals(this.metricsRollingStatisticalWindowInMilliseconds, other.metricsRollingStatisticalWindowInMilliseconds)
                && Objects.equals(this.metricsRollingStatisticalWindowBuckets, other.metricsRollingStatisticalWindowBuckets);
    }

    @Override
    public String toString() {
        return "ThreadPoolConfiguration{" +
                "threadPoolCoreSize=" + threadPoolCoreSize +
                ", keepAliveTimeMinutes=" + keepAliveTimeMinutes +
                ", maxQueueSize=" + maxQueueSize +
                ", queueSizeRejectionThreshold=" + queueSizeRejectionThreshold +
                ", metricsRollingStatisticalWindowInMilliseconds=" + metricsRollingStatisticalWindowInMilliseconds +
                ", metricsRollingStatisticalWindowBuckets=" + metricsRollingStatisticalWindowBuckets +
                '}';
    }

}
