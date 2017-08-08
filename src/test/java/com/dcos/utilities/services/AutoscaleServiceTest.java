package com.dcos.utilities.services;

import com.dcos.utilities.utils.PropsUtil;
import com.dcos.utilities.springconfiguration.properties.Props;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AutoscaleServiceTest {

    @Mock
    MarathonSDService marathonSDService;

    @Mock
    F5ScraperService f5ScraperService;

    AutoscaleService autoscaleService;
    Props props = PropsUtil.createDummyPorperties();

    @Before
    public void setUp() {
        autoscaleService = new AutoscaleService(marathonSDService, f5ScraperService, props);
    }

    @Test
    public void shouldSkipScalingIfServiceNotActiveAndClearOutLastScaleSamples() {
        F5ScraperService.F5Stats f5Stats = getF5Stats(f5ScraperService);
        f5Stats.samplesSinceLastScale = 100;
        f5Stats.serviceConfig.configuredNodes = 0;
        boolean result = autoscaleService.isScalable(f5Stats);
        assertThat(result, is(false));
        assertThat(f5Stats.samplesSinceLastScale, is(0L));
    }

    @Test
    public void shouldSkipScalingIfNotEnoughSamples() {
        F5ScraperService.F5Stats f5Stats = getF5Stats(f5ScraperService);
        f5Stats.serviceConfig.configuredNodes = 1;
        f5Stats.serviceConfig.noOfSamples = 2;
        boolean result = autoscaleService.isScalable(f5Stats);
        assertThat(result, is(false));
    }

    @Test
    public void shouldSkipScalingIfCoolDownIsMoreThanLastScale() {
        F5ScraperService.F5Stats f5Stats = getF5Stats(f5ScraperService);
        f5Stats.serviceConfig.configuredNodes = 1;
        f5Stats.samples.add(10.0);
        f5Stats.samples.add(12.0);
        f5Stats.samples.add(13.0);
        f5Stats.serviceConfig.noOfSamples = 2;
        f5Stats.serviceConfig.coolDownSamples = 3;
        f5Stats.samplesSinceLastScale = 2;
        boolean result = autoscaleService.isScalable(f5Stats);
        assertThat(result, is(false));
    }

    @Test
    public void shouldSkipScalingIfActiveNodesAreNotSameAsConfigured() {
        F5ScraperService.F5Stats f5Stats = getF5Stats(f5ScraperService);
        f5Stats.serviceConfig.configuredNodes = 1;
        f5Stats.activeNodes = 3;
        f5Stats.samples.add(10.0);
        f5Stats.samples.add(12.0);
        f5Stats.samples.add(13.0);
        f5Stats.serviceConfig.noOfSamples = 2;
        f5Stats.serviceConfig.coolDownSamples = 3;
        f5Stats.samplesSinceLastScale = 10;
        boolean result = autoscaleService.isScalable(f5Stats);
        assertThat(result, is(false));
    }

    @Test
    public void shouldDoScalingIfActiveNodesAreSameAsConfigured() {
        F5ScraperService.F5Stats f5Stats = getF5Stats(f5ScraperService);
        f5Stats.serviceConfig.configuredNodes = 1;
        f5Stats.activeNodes = 1;
        f5Stats.samples.add(10.0);
        f5Stats.samples.add(12.0);
        f5Stats.samples.add(13.0);
        f5Stats.serviceConfig.noOfSamples = 2;
        f5Stats.serviceConfig.coolDownSamples = 3;
        f5Stats.samplesSinceLastScale = 10;
        boolean result = autoscaleService.isScalable(f5Stats);
        assertThat(result, is(true));
    }

    @Test
    public void shouldCallScaleServiceIfRequiredNodesIsLessThanCurrent() {
        F5ScraperService f5ScraperService = new F5ScraperService(props, null, null);
        F5ScraperService.F5Stats f5Stats = getF5Stats(f5ScraperService);

        when(marathonSDService.scaleService("SOME-SERVICE", 1)).thenReturn(true);
        Map<String, F5ScraperService.F5Stats> serviceMap = new HashMap();
        serviceMap.put("SOME-SERVICE", f5Stats);
        f5Stats.serviceConfig.configuredNodes = 4;
        f5Stats.activeNodes = 4;
        f5Stats.samples.add(10.0);
        f5Stats.samples.add(12.0);
        f5Stats.samples.add(13.0);
        f5Stats.serviceConfig.noOfSamples = 2;
        f5Stats.serviceConfig.coolDownSamples = 3;
        f5Stats.samplesSinceLastScale = 10;
        autoscaleService.scaleIfRequired(serviceMap.entrySet().iterator().next());
        assertThat(f5Stats.samplesSinceLastScale, is(0L));
        verify(marathonSDService, times(1)).scaleService("SOME-SERVICE", 1);
    }

    @Test
    public void shouldCallScaleServiceIfRequiredNodesIsMoreThanCurrent() {
        F5ScraperService f5ScraperService = new F5ScraperService(props, null, null);
        F5ScraperService.F5Stats f5Stats = getF5Stats(f5ScraperService);

        when(marathonSDService.scaleService("SOME-SERVICE", 2)).thenReturn(true);
        Map<String, F5ScraperService.F5Stats> serviceMap = new HashMap();
        serviceMap.put("SOME-SERVICE", f5Stats);
        f5Stats.serviceConfig.configuredNodes = 1;
        f5Stats.activeNodes = 1;
        f5Stats.samples.add(100.0);
        f5Stats.samples.add(200.0);
        f5Stats.samples.add(300.0);
        f5Stats.serviceConfig.noOfSamples = 2;
        f5Stats.serviceConfig.rpsPerNode = 5;
        f5Stats.serviceConfig.coolDownSamples = 3;
        f5Stats.samplesSinceLastScale = 10;
        autoscaleService.scaleIfRequired(serviceMap.entrySet().iterator().next());
        assertThat(f5Stats.samplesSinceLastScale, is(0L));
        verify(marathonSDService, times(1)).scaleService("SOME-SERVICE", 2);
    }

    @Test
    public void shouldNotModifySamplesSinceLastScaleIfScaleRequestWasUnsuccessful() {
        F5ScraperService f5ScraperService = new F5ScraperService(props, null, null);
        F5ScraperService.F5Stats f5Stats = getF5Stats(f5ScraperService);

        when(marathonSDService.scaleService("SOME-SERVICE", 1)).thenReturn(false);
        Map<String, F5ScraperService.F5Stats> serviceMap = new HashMap();
        serviceMap.put("SOME-SERVICE", f5Stats);
        f5Stats.serviceConfig.configuredNodes = 4;
        f5Stats.activeNodes = 4;
        f5Stats.samples.add(10.0);
        f5Stats.samples.add(12.0);
        f5Stats.samples.add(13.0);
        f5Stats.serviceConfig.noOfSamples = 2;
        f5Stats.serviceConfig.coolDownSamples = 3;
        f5Stats.samplesSinceLastScale = 10;
        autoscaleService.scaleIfRequired(serviceMap.entrySet().iterator().next());
        assertThat(f5Stats.samplesSinceLastScale, is(10L));
        verify(marathonSDService, times(1)).scaleService("SOME-SERVICE", 1);
    }

    @Test(timeout = 1000L)
    public void shouldComeOutOfTheLoopOnThreadInterruptionFromRun() throws InterruptedException {
        AutoscaleService autoscaleService = Mockito.spy(this.autoscaleService);
        //configure loop to only run once
        doThrow(new InterruptedException()).when(autoscaleService).waitFor(props.getService().getSchedulingIntervalInSecs() * 1000L);
        try {
            assertThat(autoscaleService.isHealthy(), is(false));
            autoscaleService.run(null);
        } catch (InterruptedException ex) {
            //last returned health status when all calls were success
            assertThat(autoscaleService.isHealthy(), is(true));
            return;
        }
        assertFalse(true);
    }

    @Test(timeout = 1000L)
    public void shouldComeOutOfTheLoopOnThreadInterruptionFromRunSetHealthyFalseIfAnyCallsFailed() throws InterruptedException {
        AutoscaleService autoscaleService = Mockito.spy(this.autoscaleService);
        //configure loop to only run once
        doThrow(new InterruptedException()).when(autoscaleService).waitFor(props.getService().getSchedulingIntervalInSecs() * 1000L);
        //failing call when getting data from marathon
        doThrow(new RuntimeException()).when(marathonSDService).updateServiceData();
        try {
            assertThat(autoscaleService.isHealthy(), is(false));
            autoscaleService.run(null);
        } catch (InterruptedException ex) {
            //last returned health status when all calls were failure
            assertThat(autoscaleService.isHealthy(), is(false));
            return;
        }
        assertFalse(true);
    }


    private F5ScraperService.F5Stats getF5Stats(F5ScraperService f5ScraperService) {
        Map labelsMap = new HashMap();
        labelsMap.put("AUTOSCALE", "true");
        F5ScraperService.F5Stats f5Stats = f5ScraperService.new F5Stats();
        Map<String, Object> serviceConfigMap = new HashMap();
        serviceConfigMap.put("labels", labelsMap);
        serviceConfigMap.put("instances", 0);
        f5Stats.serviceConfig = marathonSDService.new ServiceConfig(serviceConfigMap);
        return f5Stats;
    }
}
