package com.dcos.utilities.monitoring;

import com.dcos.utilities.services.AutoscaleService;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoscaleServiceExports extends Collector {
    private final Node node;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CollectorRegistry registry;
    private final AutoscaleService autoscaleService;

    public AutoscaleServiceExports(Node node, CollectorRegistry registry, AutoscaleService autoscaleService) {
        this.node = node;
        this.registry = registry;
        this.autoscaleService = autoscaleService;
    }

    @Override
    public List<MetricFamilySamples> collect() {

        final List<MetricFamilySamples> mfs = new ArrayList<>();
        GaugeMetricFamily cacheServerHealth = new GaugeMetricFamily(Constants.AUTOSCALE_SERVICE_DEPENDENCY
                , "Monitors the health of the autoscale service",
                Collections.singletonList("node_name"));
        try {
            cacheServerHealth.addMetric(Collections.singletonList(node.name), autoscaleService.isHealthy() ? 1 : 0);
        } catch (Exception e) {
            cacheServerHealth.addMetric(Collections.singletonList(node.name), 0);
            LOG.error("Error to call autoscale service :", e);
        }

        mfs.add(cacheServerHealth);
        return mfs;
    }

       @Override
    public AutoscaleServiceExports register() {
        registry.register(this);
        return this;
    }

}
