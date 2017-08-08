package com.dcos.utilities.gateways;

import com.dcos.utilities.springconfiguration.properties.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;

@Component
public class F5MetricsGateway {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Props props;
    boolean isHealthy = false;

    public F5MetricsGateway(Props props) {

        this.props = props;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public String getF5ScraperMetrics() {
        String response = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.getForObject(props.getF5Exporter().getUrl(), String.class);
            isHealthy = true;
        } catch (RestClientException e) {
            LOG.error("Error in calling F5 scraper: ", e);
            isHealthy = false;
            response = null;
        }
        return response;
    }
}
