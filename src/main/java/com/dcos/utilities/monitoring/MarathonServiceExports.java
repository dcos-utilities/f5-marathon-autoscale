package com.dcos.utilities.monitoring;

import com.dcos.utilities.gateways.MarathonGateway;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarathonServiceExports extends Collector {
    private final Node node;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CollectorRegistry registry;
    private MarathonGateway marathonGateway;

    public MarathonServiceExports(Node node, CollectorRegistry registry, MarathonGateway marathonGateway) {
        this.node = node;
        this.registry = registry;
        this.marathonGateway = marathonGateway;
    }

    @Override
    public List<MetricFamilySamples> collect() {

        final List<MetricFamilySamples> mfs = new ArrayList<>();
        GaugeMetricFamily cacheServerHealth = new GaugeMetricFamily(Constants.MARATHON_SERVICE_DEPENDENCY
                , "Monitors the health of the marathon service",
                Collections.singletonList("node_name"));
        try {
            cacheServerHealth.addMetric(Collections.singletonList(node.name), marathonGateway.isHealthy() ? 1 : 0);
        } catch (Exception e) {
            cacheServerHealth.addMetric(Collections.singletonList(node.name), 0);
            LOG.error("Error to call docker registry :", e);
        }

        mfs.add(cacheServerHealth);
        return mfs;
    }

       @Override
    public MarathonServiceExports register() {
        registry.register(this);
        return this;
    }

}
