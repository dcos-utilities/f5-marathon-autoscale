package com.dcos.utilities.gateways;

import com.dcos.utilities.springconfiguration.properties.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.Map;

@Component
public class MarathonGateway {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Props props;
    boolean isHealthy = false;

    public MarathonGateway(Props props) {
        this.props = props;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public String getMarathonServiceDetails() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity =
                    restTemplate.exchange(props.getMarathon().getUrl() + "/v2/apps", HttpMethod.GET, null, String.class);

            if (responseEntity.getStatusCode().value() != 200) {
                LOG.error("error marathon service failed with status code " + responseEntity.getStatusCode().value());
                return null;
            }
            isHealthy = true;
            if (LOG.isTraceEnabled()) {
                LOG.trace("marathon services details: " + responseEntity.getBody());
            }
            return responseEntity.getBody();
        } catch (RestClientException e) {
            LOG.error("error in calling marathon service details: ", e);
            isHealthy = false;
            return null;
        }
    }


    public boolean scaleService(String serviceId, int instances) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("scale serviceId: " + serviceId + " instances: " + instances);
            }
            RestTemplate restTemplate = new RestTemplate();
            String request = "{\"instances\" : " + instances + "}";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            HttpEntity<String> requestEntity = new HttpEntity(request, headers);
            ResponseEntity<Map> responseEntity =
                    restTemplate.exchange(props.getMarathon().getUrl() + "/v2/apps/" + serviceId, HttpMethod.PUT, requestEntity, Map.class);
            isHealthy = true;

            if (LOG.isTraceEnabled()) {
                LOG.trace("service scale response: " + responseEntity.getBody());
            }
            if (responseEntity.getStatusCode().value() != 200) {
                LOG.error("error in Scale serviceId: " + serviceId + " instances: " + instances + " http status code: " + responseEntity.getStatusCode().value());
                return false;
            }
            return true;
        } catch (RestClientException e) {
            LOG.error("error in Scale serviceId: " + serviceId + " instances: " + instances, e);
            isHealthy = false;
            return false;
        }

    }
}
