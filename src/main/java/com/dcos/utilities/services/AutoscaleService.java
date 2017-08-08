package com.dcos.utilities.services;

import com.dcos.utilities.springconfiguration.properties.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Map;


@Component
public class AutoscaleService implements ApplicationRunner {

    private MarathonSDService marathonSDService;
    private F5ScraperService f5ScraperService;
    private Props props;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private boolean isHealthy;

    @Inject
    public AutoscaleService(MarathonSDService marathonSDService, F5ScraperService f5ScraperService, Props props) {
        this.marathonSDService = marathonSDService;
        this.f5ScraperService = f5ScraperService;
        this.props = props;
    }


    void scale(Map.Entry<String, F5ScraperService.F5Stats> stats, int instances) {
        LOG.info("scale serviceid: " + stats.getKey() + " instances: " + instances);
        boolean scale = marathonSDService.scaleService(stats.getKey(), instances);
        if (scale) {
            stats.getValue().samplesSinceLastScale = 0;
        } else {
            LOG.error("error scale service id: " + stats.getKey() + " instances: " + instances);
        }
    }

    void scaleIfRequired(Map.Entry<String, F5ScraperService.F5Stats> stats) {
        F5ScraperService.F5Stats statsObj = stats.getValue();
        int requireNodes = (int) (statsObj.overallAverageRps() / statsObj.serviceConfig.rpsPerNode);
        LOG.info("ServiceId: " + stats.getKey() + " requireNodes: " + requireNodes + " overallAverageRps: " + statsObj.overallAverageRps() + " rpsPerNode: " + statsObj.serviceConfig.rpsPerNode);
        if (requireNodes < 0) {
            LOG.warn("Scaling nodes value is coming negative for service: " + stats);
            return;
        }
        if (requireNodes < statsObj.serviceConfig.minNodes) {
            requireNodes = statsObj.serviceConfig.minNodes;
        } else if (requireNodes > statsObj.serviceConfig.maxNodes) {
            requireNodes = statsObj.serviceConfig.maxNodes;
        }
        if (requireNodes == statsObj.serviceConfig.configuredNodes) {
            return;
        }
        scale(stats, requireNodes);
    }

    boolean isScalable(F5ScraperService.F5Stats statsObj) {
        //first check if the service is stopped or not
        if (statsObj.serviceConfig.configuredNodes == 0) {
            statsObj.samplesSinceLastScale = 0;
            return false;
        }

        //checking for appropriate samples being available
        if (statsObj.serviceConfig.noOfSamples > statsObj.samples.size()) {
            return false;
        }

        if (statsObj.serviceConfig.coolDownSamples > statsObj.samplesSinceLastScale) {
            return false;
        }

        if (statsObj.activeNodes != statsObj.serviceConfig.configuredNodes) {
            LOG.warn("Service not able to scale up/down. Maybe the cool down time is not configured correctly");
            return false;
        }
        return true;
    }

    void waitFor(long millis) throws InterruptedException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.error("loop interrupted", e);
            throw e; // stop service if interrupted automatic restart
        }
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        while (true) {
            try {
                if(LOG.isDebugEnabled()) {
                    LOG.debug(" In Run: " + System.currentTimeMillis());
                }
                marathonSDService.updateServiceData();
                f5ScraperService.updateF5Stats();
                for (Map.Entry<String, F5ScraperService.F5Stats> stats : f5ScraperService.getF5StatsMap().entrySet()) {
                    LOG.info("Service id: " + stats.getKey()
                            + " serviceConfig: " + stats.getValue());
                    if (isScalable(stats.getValue())) {
                        scaleIfRequired(stats);
                    }
                }
                isHealthy = true;
            } catch (Exception e) {
                isHealthy = false;
                LOG.error("Exception", e);
            } finally {
                waitFor(props.getService().getSchedulingIntervalInSecs() * 1000);
            }
        }
    }


    public boolean isHealthy() {
        return isHealthy;
    }
}
