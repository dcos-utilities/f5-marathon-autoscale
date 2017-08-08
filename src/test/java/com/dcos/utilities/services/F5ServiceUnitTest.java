package com.dcos.utilities.services;

import com.dcos.utilities.utils.PropsUtil;
import com.dcos.utilities.gateways.F5MetricsGateway;
import com.dcos.utilities.gateways.MarathonGateway;
import com.dcos.utilities.springconfiguration.properties.Props;
import com.dcos.utilities.utils.Fixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class F5ServiceUnitTest {

    @Mock
    private MarathonGateway marathonGateway;
    @Mock
    private F5MetricsGateway f5MetricsGateway;

    private MarathonSDService marathonSDService;
    private F5ScraperService f5ScraperService;
    private Props props = PropsUtil.createDummyPorperties();

    @Before
    public void setUp() {
        marathonSDService = new MarathonSDService(marathonGateway);
        f5ScraperService = new F5ScraperService(props, marathonSDService, f5MetricsGateway);
    }

    @Test
    public void shouldNotReturnServiceDetailsIfNoAutoScaleServiceConfiguredOnMarathon() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/noautoscaleservice_without_label_section.json");
        String f5Response = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_1.txt");

        Mockito.when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response);

        marathonSDService.updateServiceData();
        f5ScraperService.updateF5Stats();

        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(0));
        assertThat(f5ScraperService.getF5StatsMap().size(), is(0));

        verify(f5MetricsGateway, times(1)).getF5ScraperMetrics();
    }

    @Test
    public void shouldReturnSingleServiceDetailsIfSingleAutoScaleServiceConfiguredOnMarathon() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/singleautoscaleservice_withconfig.json");
        String f5Response = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_1.txt");

        Mockito.when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response);

        marathonSDService.updateServiceData();
        f5ScraperService.updateF5Stats();

        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().keySet().contains("/z-donot-delete/nginx"), is(true));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samplesSinceLastScale, is(1L));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").activeNodes, is(2));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.getFirst(), is(2.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").serviceConfig, is(marathonSDService.getServiceDetails("/z-donot-delete/nginx")));

        verify(f5MetricsGateway, times(1)).getF5ScraperMetrics();
    }

    @Test
    public void shouldReturnSingleServiceDetailsIfMultipleAutoScaleServicesConfiguredOnMarathon() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/multipleautoscaleservices_withoutconfig.json");
        String f5Response = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_1.txt");

        Mockito.when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response);

        marathonSDService.updateServiceData();
        f5ScraperService.updateF5Stats();

        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(2));
        assertThat(f5ScraperService.getF5StatsMap().size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().keySet().contains("/z-donot-delete/nginx"), is(true));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samplesSinceLastScale, is(1L));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").activeNodes, is(2));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.getFirst(), is(2.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").serviceConfig, is(marathonSDService.getServiceDetails("/z-donot-delete/nginx")));

        verify(f5MetricsGateway, times(1)).getF5ScraperMetrics();
    }

    @Test
    public void shouldReturnMultipleServicesDetailsIfMultipleAutoScaleServicesConfiguredOnMarathon() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/multipleautoscaleservices_withoutconfig.json");
        String f5Response = Fixtures.getJSONFromFile("f5/f5scrapresponse_multiple_autoscale_1.txt");

        Mockito.when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response);

        marathonSDService.updateServiceData();
        f5ScraperService.updateF5Stats();

        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(2));
        assertThat(f5ScraperService.getF5StatsMap().size(), is(2));

        assertThat(f5ScraperService.getF5StatsMap().keySet().contains("/z-donot-delete/nginx"), is(true));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samplesSinceLastScale, is(1L));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").activeNodes, is(2));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.getFirst(), is(2.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").serviceConfig, is(marathonSDService.getServiceDetails("/z-donot-delete/nginx")));

        assertThat(f5ScraperService.getF5StatsMap().keySet().contains("/z-donot-delete/webservice"), is(true));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samplesSinceLastScale, is(1L));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/webservice").activeNodes, is(5));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/webservice").samples.size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/webservice").samples.getFirst(), is(400.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/webservice").serviceConfig, is(marathonSDService.getServiceDetails("/z-donot-delete/webservice")));


        verify(f5MetricsGateway, times(1)).getF5ScraperMetrics();
    }


    @Test
    public void shouldReturnSingleServiceDetailsWithMultipleSamplesIfSingleAutoScaleServicesConfiguredOnMarathon() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/singleautoscaleservice_withconfig.json");
        String f5Response_1 = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_1.txt");
        String f5Response_2 = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_2.txt");

        Mockito.when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response_1);

        marathonSDService.updateServiceData();
        f5ScraperService.updateF5Stats();

        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response_2);
        f5ScraperService.updateF5Stats();

        assertThat(f5ScraperService.getF5StatsMap().size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().keySet().contains("/z-donot-delete/nginx"), is(true));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").activeNodes, is(2));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samplesSinceLastScale, is(2L));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.size(), is(2));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.getFirst(), is(2.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.getLast(), is(5.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").serviceConfig, is(marathonSDService.getServiceDetails("/z-donot-delete/nginx")));

        verify(f5MetricsGateway, times(2)).getF5ScraperMetrics();
    }

    @Test
    public void shouldReturnSingleServiceDetailsWithThreshHoldSamplesIfSIngleAutoScaleServicesConfiguredOnMarathon() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/singleautoscaleservice_withconfig.json");
        String f5Response_1 = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_1.txt");
        String f5Response_2 = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_2.txt");
        String f5Response_3 = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_3.txt");
        String f5Response_4 = Fixtures.getJSONFromFile("f5/f5scrapresponse_single_autoscale_4.txt");

        Mockito.when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        marathonSDService.updateServiceData();

        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response_1);
        f5ScraperService.updateF5Stats();

        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response_2);
        f5ScraperService.updateF5Stats();

        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response_3);
        f5ScraperService.updateF5Stats();

        Mockito.when(f5MetricsGateway.getF5ScraperMetrics()).thenReturn(f5Response_4);
        f5ScraperService.updateF5Stats();

        assertThat(f5ScraperService.getF5StatsMap().size(), is(1));
        assertThat(f5ScraperService.getF5StatsMap().keySet().contains("/z-donot-delete/nginx"), is(true));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").activeNodes, is(2));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.size(), is(3));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samplesSinceLastScale, is(4L));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.get(0), is(5.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.get(1), is(20.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").samples.get(2), is(40.0));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").overallAverageRps(), is((40.0-5.0)/(2*10)));
        assertThat(f5ScraperService.getF5StatsMap().get("/z-donot-delete/nginx").serviceConfig, is(marathonSDService.getServiceDetails("/z-donot-delete/nginx")));

        verify(f5MetricsGateway, times(4)).getF5ScraperMetrics();
    }
}
