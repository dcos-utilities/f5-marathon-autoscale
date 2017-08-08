package com.dcos.utilities.monitoring;

import com.dcos.utilities.gateways.F5MetricsGateway;
import com.dcos.utilities.gateways.MarathonGateway;
import com.dcos.utilities.services.AutoscaleService;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConditionalOnClass(CollectorRegistry.class)
public class PrometheusMetricsConfiguration {

    final static CollectorRegistry COLLECTOR_REGISTRY = new CollectorRegistry(true);

    @Bean
    @ConditionalOnMissingBean
    CollectorRegistry metricRegistry() {
        return COLLECTOR_REGISTRY;
    }

    @Bean
    ServletRegistrationBean registerPrometheusExporterServlet(CollectorRegistry metricRegistry) {
        return new ServletRegistrationBean(new MetricsServlet(COLLECTOR_REGISTRY), "/metrics");
    }

    @Bean
    ExporterRegister exporterRegister(F5MetricsGateway f5MetricsGateway, MarathonGateway marathonGateway, AutoscaleService autoscaleService) {
        COLLECTOR_REGISTRY.clear();
        List<Collector> collectors = new ArrayList<>();
        Node node = new Node(System.getenv("NODE_NAME"));
        collectors.addAll(Arrays.asList(
                new MarathonServiceExports(node, COLLECTOR_REGISTRY, marathonGateway),
                new F5ScraperServiceExports(node, COLLECTOR_REGISTRY, f5MetricsGateway),
                new AutoscaleServiceExports(node, COLLECTOR_REGISTRY, autoscaleService)));
        ExporterRegister register = new ExporterRegister(collectors);
        return register;
    }

}
