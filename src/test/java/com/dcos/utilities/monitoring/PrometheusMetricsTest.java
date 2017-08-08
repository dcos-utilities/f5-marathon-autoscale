package com.dcos.utilities.monitoring;

import com.dcos.utilities.gateways.F5MetricsGateway;
import com.dcos.utilities.gateways.MarathonGateway;
import com.dcos.utilities.services.AutoscaleService;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PrometheusMetricsTest {

    private static final String NODE_NAME = "localhost";

    @Mock
    CollectorRegistry registry;

    @Mock
    F5MetricsGateway f5MetricsGateway;

    @Mock
    private MarathonGateway marathonGateway;

    @Mock
    private AutoscaleService autoscaleService;


    @Test
    public void shouldProvideF5ScraperDependencyUp() throws Exception{
        Node node = new Node(NODE_NAME);
        F5ScraperServiceExports f5ScraperServiceExports = new F5ScraperServiceExports(node,registry,f5MetricsGateway);
        when(f5MetricsGateway.isHealthy()).thenReturn(true);

        final List<Collector.MetricFamilySamples> collect = f5ScraperServiceExports.collect();
        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0).samples.get(0).name).isEqualTo("f5_scraper_service_health");
        assertThat(collect.get(0).samples.get(0).labelNames).isEqualTo(Collections.singletonList("node_name"));
        assertThat(collect.get(0).samples.get(0).labelValues).isEqualTo(Collections.singletonList(NODE_NAME));
        assertThat(collect.get(0).samples.get(0).value).isEqualTo(1.0);
        verify(f5MetricsGateway, times(1)).isHealthy();
    }

    @Test
    public void shouldProvideF5ScraperDependencyDown() throws Exception{
        Node node = new Node(NODE_NAME);
        F5ScraperServiceExports f5ScraperServiceExports = new F5ScraperServiceExports(node,registry,f5MetricsGateway);
        when(f5MetricsGateway.isHealthy()).thenReturn(false);

        final List<Collector.MetricFamilySamples> collect = f5ScraperServiceExports.collect();
        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0).samples.get(0).name).isEqualTo("f5_scraper_service_health");
        assertThat(collect.get(0).samples.get(0).labelNames).isEqualTo(Collections.singletonList("node_name"));
        assertThat(collect.get(0).samples.get(0).labelValues).isEqualTo(Collections.singletonList(NODE_NAME));
        assertThat(collect.get(0).samples.get(0).value).isEqualTo(0.0);
        verify(f5MetricsGateway, times(1)).isHealthy();
    }

    @Test
    public void shouldProvideMarathonScraperDependencyUp() throws Exception{
        Node node = new Node(NODE_NAME);
        MarathonServiceExports marathonServiceExports = new MarathonServiceExports(node,registry,marathonGateway);
        when(marathonGateway.isHealthy()).thenReturn(true);

        final List<Collector.MetricFamilySamples> collect = marathonServiceExports.collect();
        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0).samples.get(0).name).isEqualTo("marathon_service_health");
        assertThat(collect.get(0).samples.get(0).labelNames).isEqualTo(Collections.singletonList("node_name"));
        assertThat(collect.get(0).samples.get(0).labelValues).isEqualTo(Collections.singletonList(NODE_NAME));
        assertThat(collect.get(0).samples.get(0).value).isEqualTo(1.0);
        verify(marathonGateway, times(1)).isHealthy();
    }

    @Test
    public void shouldProvideMarathonScraperDependencyDown() throws Exception{
        Node node = new Node(NODE_NAME);
        MarathonServiceExports marathonServiceExports = new MarathonServiceExports(node,registry,marathonGateway);
        when(marathonGateway.isHealthy()).thenReturn(false);

        final List<Collector.MetricFamilySamples> collect = marathonServiceExports.collect();
        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0).samples.get(0).name).isEqualTo("marathon_service_health");
        assertThat(collect.get(0).samples.get(0).labelNames).isEqualTo(Collections.singletonList("node_name"));
        assertThat(collect.get(0).samples.get(0).labelValues).isEqualTo(Collections.singletonList(NODE_NAME));
        assertThat(collect.get(0).samples.get(0).value).isEqualTo(0.0);
        verify(marathonGateway, times(1)).isHealthy();
    }

    @Test
    public void shouldProvideAutoscaleDependencyUp() throws Exception{
        Node node = new Node(NODE_NAME);
        AutoscaleServiceExports autoscaleServiceExports = new AutoscaleServiceExports(node,registry,autoscaleService);
        when(autoscaleService.isHealthy()).thenReturn(true);

        final List<Collector.MetricFamilySamples> collect = autoscaleServiceExports.collect();
        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0).samples.get(0).name).isEqualTo("autoscale_service_health");
        assertThat(collect.get(0).samples.get(0).labelNames).isEqualTo(Collections.singletonList("node_name"));
        assertThat(collect.get(0).samples.get(0).labelValues).isEqualTo(Collections.singletonList(NODE_NAME));
        assertThat(collect.get(0).samples.get(0).value).isEqualTo(1.0);
        verify(autoscaleService, times(1)).isHealthy();
    }

    @Test
    public void shouldProvideAutoscaleDependencyDown() throws Exception{
        Node node = new Node(NODE_NAME);
        AutoscaleServiceExports autoscaleServiceExports = new AutoscaleServiceExports(node,registry,autoscaleService);
        when(marathonGateway.isHealthy()).thenReturn(false);

        final List<Collector.MetricFamilySamples> collect = autoscaleServiceExports.collect();
        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0).samples.get(0).name).isEqualTo("autoscale_service_health");
        assertThat(collect.get(0).samples.get(0).labelNames).isEqualTo(Collections.singletonList("node_name"));
        assertThat(collect.get(0).samples.get(0).labelValues).isEqualTo(Collections.singletonList(NODE_NAME));
        assertThat(collect.get(0).samples.get(0).value).isEqualTo(0.0);
        verify(autoscaleService, times(1)).isHealthy();
    }
}
