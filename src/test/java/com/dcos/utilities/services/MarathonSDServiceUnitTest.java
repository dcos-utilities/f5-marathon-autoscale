package com.dcos.utilities.services;

import com.dcos.utilities.gateways.MarathonGateway;
import com.dcos.utilities.utils.Fixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MarathonSDServiceUnitTest {

    @Mock
    private MarathonGateway marathonGateway;
    private MarathonSDService marathonSDService;

    @Before
    public void setUp() {
        marathonSDService = new MarathonSDService(marathonGateway);
    }

    @Test
    public void shouldNotReturnServiceDetailsIfLabelSectionNotPresent() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/noautoscaleservice_without_label_section.json");
        Mockito.when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        marathonSDService.updateServiceData();
        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(0));
        verify(marathonGateway, times(1)).getMarathonServiceDetails();
    }

    @Test
    public void shouldNotReturnServiceDetailsIfLabelSectionPresentWithOutlabel() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/noautoscaleservice_with_label_section_without_label.json");
        when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        marathonSDService.updateServiceData();
        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(0));
        verify(marathonGateway, times(1)).getMarathonServiceDetails();
    }

    @Test
    public void shouldNotReturnServiceDetailsIfLabelSectionPresentWithOtherlabel() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/noautoscaleservice_with_label_section_with_other_label.json");
        when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        marathonSDService.updateServiceData();
        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(0));
        verify(marathonGateway, times(1)).getMarathonServiceDetails();
    }

    @Test
    public void shouldNotReturnServiceDetailsIfLabelSectionPresentWithAutoScalelabelWIthNonTrueValue() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/noautoscaleservice_with_label_section_with_autoscalelabel_not_true_value.json");
        when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        marathonSDService.updateServiceData();
        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(0));
        verify(marathonGateway, times(1)).getMarathonServiceDetails();
    }

    @Test
    public void shouldReturnServiceDetailsIfLabelSectionPresentWithAutoScaleConfiguration() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/singleautoscaleservice_withconfig.json");
        when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        marathonSDService.updateServiceData();
        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(1));
        assertThat(marathonSDService.getAutoScaleServicesNames().contains("/z-donot-delete/nginx"), is(true));
        final MarathonSDService.ServiceConfig serviceDetails = marathonSDService.getServiceDetails("/z-donot-delete/nginx");
        assertThat(serviceDetails.configuredNodes, is(2));
        assertThat(serviceDetails.coolDownSamples, is(2));
        assertThat(serviceDetails.minNodes, is(2));
        assertThat(serviceDetails.noOfSamples, is(2));
        assertThat(serviceDetails.maxNodes, is(5));
        assertThat(serviceDetails.rpsPerNode, is(5));
        verify(marathonGateway, times(1)).getMarathonServiceDetails();
    }

    @Test
    public void shouldReturnMultipleServiceDetailsIfLabelSectionPresentWithAutoScaleNoConfiguration() throws Exception {
        String marathonResponse = Fixtures.getJSONFromFile("marathon/multipleautoscaleservices_withoutconfig.json");
        when(marathonGateway.getMarathonServiceDetails()).thenReturn(marathonResponse);
        marathonSDService.updateServiceData();
        assertThat(marathonSDService.getAutoScaleServicesNames().size(), is(2));
        assertThat(marathonSDService.getAutoScaleServicesNames().contains("/z-donot-delete/nginx"), is(true));
        assertThat(marathonSDService.getAutoScaleServicesNames().contains("/z-donot-delete/webservice"), is(true));
        MarathonSDService.ServiceConfig serviceDetails = marathonSDService.getServiceDetails("/z-donot-delete/nginx");
        assertThat(serviceDetails.configuredNodes, is(2));
        assertThat(serviceDetails.coolDownSamples, is(30));
        assertThat(serviceDetails.minNodes, is(1));
        assertThat(serviceDetails.noOfSamples, is(10));
        assertThat(serviceDetails.maxNodes, is(5));
        assertThat(serviceDetails.rpsPerNode, is(100));

        serviceDetails = marathonSDService.getServiceDetails("/z-donot-delete/webservice");
        assertThat(serviceDetails.configuredNodes, is(10));
        assertThat(serviceDetails.maxNodes, is(5));
        verify(marathonGateway, times(1)).getMarathonServiceDetails();
    }
}
