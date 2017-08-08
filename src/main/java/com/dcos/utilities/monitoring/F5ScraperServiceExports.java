package com.dcos.utilities.monitoring;

import com.dcos.utilities.gateways.F5MetricsGateway;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class F5ScraperServiceExports extends Collector {
    private final Node node;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CollectorRegistry registry;
    private F5MetricsGateway f5MetricsGateway;

    public F5ScraperServiceExports(Node node, CollectorRegistry registry, F5MetricsGateway f5MetricsGateway) {
        this.node = node;
        this.registry = registry;
        this.f5MetricsGateway = f5MetricsGateway;
    }

    @Override
    public List<MetricFamilySamples> collect() {

        final List<MetricFamilySamples> mfs = new ArrayList<>();
        GaugeMetricFamily cacheServerHealth = new GaugeMetricFamily(Constants.F5_SCRAPER_SERVICE_DEPENDENCY
                , "Monitors the health of the f5 scraper service",
                Collections.singletonList("node_name"));
        try {
            cacheServerHealth.addMetric(Collections.singletonList(node.name), f5MetricsGateway.isHealthy() ? 1 : 0);
        } catch (Exception e) {
            cacheServerHealth.addMetric(Collections.singletonList(node.name), 0);
            LOG.error("Error to call f5MetricsGateway :", e);
        }

        mfs.add(cacheServerHealth);
        return mfs;
    }

       @Override
    public F5ScraperServiceExports register() {
        registry.register(this);
        return this;
    }

}
