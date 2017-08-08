package com.dcos.utilities.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.dcos.utilities.gateways.MarathonGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Component
public class MarathonSDService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private MarathonGateway marathonGateway;

    private final Filter filter = Filter.filter(Criteria.where("labels.AUTOSCALE").eq("true"));
    private Map<String, ServiceConfig> serviceConfigMap = new HashMap();

    @Inject
    public MarathonSDService(MarathonGateway marathonGateway) {
        this.marathonGateway = marathonGateway;
    }

    public void updateServiceData() {
        serviceConfigMap.clear();
        String serviceData = marathonGateway.getMarathonServiceDetails();

        Map<String, String> myMap = new HashMap<String, String>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            myMap = objectMapper.readValue(serviceData.getBytes(), HashMap.class);
            List<Map<String, Object>> services = JsonPath.read(myMap, "$['apps'][?]", filter);
            for (Map service : services) {
                String serviceId = (String) service.get("id");
                LOG.debug("parsing service metadata: " + serviceId);
                serviceConfigMap.put(serviceId, new ServiceConfig(service));
            }

        } catch (IOException e) {
            LOG.error("error in updateServiceData :", e);
        }
    }

    public Set<String> getAutoScaleServicesNames() {
        return serviceConfigMap.keySet();
    }

    public ServiceConfig getServiceDetails(String serviceId) {
        return serviceConfigMap.get(serviceId);
    }

    class ServiceConfig {
        int rpsPerNode;
        int noOfSamples;
        int coolDownSamples;
        int minNodes;
        int maxNodes;
        int configuredNodes;

        public ServiceConfig(Map service) {
            this.configuredNodes = (Integer) service.get("instances");
            Map serviceLabels = (Map) service.get("labels");
            this.rpsPerNode = getOrDefault(serviceLabels, "AUTOSCALE_RPS_PER_NODE", 100);
            this.noOfSamples = getOrDefault(serviceLabels, "AUTOSCALE_NO_OF_SAMPLES", 10);
            this.coolDownSamples = getOrDefault(serviceLabels, "AUTOSCALE_COOLDOWN_SAMPLES", 30);
            this.minNodes = getOrDefault(serviceLabels, "AUTOSCALE_MINIMUM_NODES", 1);
            this.maxNodes = getOrDefault(serviceLabels, "AUTOSCALE_MAXIMUM_NODES", 5);
        }

        private int getOrDefault(Map service, String key, int defaultValue) {
            if (service.containsKey(key)) {
                try {
                    return Integer.parseInt((String) service.get(key));
                } catch (NumberFormatException ne) {
                    LOG.error("error parsing service metadata :" + key, ne);
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        }

        @Override
        public String toString() {
            return "ServiceConfig{" +
                    "rpsPerNode=" + rpsPerNode +
                    ", noOfSamples=" + noOfSamples +
                    ", coolDownSamples=" + coolDownSamples +
                    ", minNodes=" + minNodes +
                    ", maxNodes=" + maxNodes +
                    ", configuredNodes=" + configuredNodes +
                    '}';
        }
    }

    public boolean scaleService(String serviceId, int instances) {
        return marathonGateway.scaleService(serviceId, instances);
    }

}
