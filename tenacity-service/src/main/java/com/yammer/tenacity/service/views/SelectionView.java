package com.yammer.tenacity.service.views;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.netflix.turbine.discovery.ConfigPropertyBasedDiscovery;
import com.netflix.turbine.discovery.Instance;
import com.yammer.dropwizard.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectionView extends View {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionView.class);

    public SelectionView() {
        super("/templates/selection/selection.mustache");
    }

    public ImmutableCollection<String> getClusters() {
        final ImmutableSet.Builder<String> clusters = ImmutableSet.builder();
        final ConfigPropertyBasedDiscovery configPropertyBasedDiscovery = new ConfigPropertyBasedDiscovery();
        try {
            for (Instance instance : configPropertyBasedDiscovery.getInstanceList()) {
                clusters.add(instance.getCluster());
            }
        } catch (Exception err) {
            LOGGER.warn("Could not fetch clusters dynamically", err);
        }

        return Ordering.natural().immutableSortedCopy(clusters.build());
    }
}