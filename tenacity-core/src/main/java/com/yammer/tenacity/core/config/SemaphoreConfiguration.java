package com.yammer.tenacity.core.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Objects;

public class SemaphoreConfiguration {
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int maxConcurrentRequests = 10;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int fallbackMaxConcurrentRequests = 10;

    public SemaphoreConfiguration() {}

    public SemaphoreConfiguration(int maxConcurrentRequests,
                                  int fallbackMaxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.fallbackMaxConcurrentRequests = fallbackMaxConcurrentRequests;
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    public int getFallbackMaxConcurrentRequests() {
        return fallbackMaxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    public void setFallbackMaxConcurrentRequests(int fallbackMaxConcurrentRequests) {
        this.fallbackMaxConcurrentRequests = fallbackMaxConcurrentRequests;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxConcurrentRequests, fallbackMaxConcurrentRequests);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SemaphoreConfiguration other = (SemaphoreConfiguration) obj;
        return Objects.equals(this.maxConcurrentRequests, other.maxConcurrentRequests)
                && Objects.equals(this.fallbackMaxConcurrentRequests, other.fallbackMaxConcurrentRequests);
    }

    @Override
    public String toString() {
        return "SemaphoreConfiguration{" +
                "maxConcurrentRequests=" + maxConcurrentRequests +
                ", fallbackMaxConcurrentRequests=" + fallbackMaxConcurrentRequests +
                '}';
    }
}