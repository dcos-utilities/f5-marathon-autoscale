package com.dcos.utilities.services;

import com.dcos.utilities.gateways.F5MetricsGateway;
import com.dcos.utilities.springconfiguration.properties.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class F5ScraperService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Pattern pattern = Pattern.compile(".*pool=\"(.*?)[0-9._]+\".*");
    private static final String bigip_pool_tot_requests = "bigip_pool_tot_requests";
    private static final String bigip_pool_active_member_cnt = "bigip_pool_active_member_cnt";
    private Set<String> metricsNames = new HashSet(Arrays.asList(new String[]{bigip_pool_tot_requests, bigip_pool_active_member_cnt}));
    private Map<String, F5Stats> f5StatsMap = new HashMap();
    private MarathonSDService marathonSDService;
    private F5MetricsGateway f5MetricsGateway;
    private Props props;

    @Inject
    public F5ScraperService(Props props, MarathonSDService marathonSDService, F5MetricsGateway f5MetricsGateway) {
        this.props = props;
        this.marathonSDService = marathonSDService;
        this.f5MetricsGateway = f5MetricsGateway;
    }

    public void updateF5Stats() {
        Set<String> autoScaleServices = marathonSDService.getAutoScaleServicesNames();
        String f5ScraperResponse = f5MetricsGateway.getF5ScraperMetrics();
        try {
            Scanner scanner = new Scanner(f5ScraperResponse);
            while (scanner.hasNext()) {
                String F5Metric = scanner.nextLine().trim();
                LOG.debug("F5Metric: " + F5Metric);
                //Ignore comment
                if (F5Metric.startsWith("#") || F5Metric.trim().equals("")) {
                    continue;
                }
                // Metric is for Interest ?
                String metricName = getMetricName(F5Metric);
                LOG.debug("metricName: " + metricName);
                if (metricName == null || !metricsNames.contains(metricName)) {
                    continue;
                }
                // Service is for Interest ?
                String serviceName = getServiceName(F5Metric);
                LOG.debug("serviceName: " + serviceName);
                if (serviceName == null || !autoScaleServices.contains(serviceName)) {
                    continue;
                }
                //Metric Value
                Double metricValue = getMetricValue(F5Metric);
                if (metricValue == null) {
                    continue;
                }
                F5Stats f5Stats = f5StatsMap.get(serviceName);
                if (f5Stats == null) {
                    f5Stats = new F5Stats();
                    f5StatsMap.put(serviceName, f5Stats);
                }
                f5Stats.serviceConfig = marathonSDService.getServiceDetails(serviceName);

                if (metricName.equals(bigip_pool_tot_requests)) {
                    f5Stats.addTotalRequest(metricValue);
                } else if (metricName.equals(bigip_pool_active_member_cnt)) {
                    f5Stats.activeNodes = metricValue.intValue();
                }

            }
            scanner.close();

        } catch (Exception e) {
            LOG.error("Exception in updateF5Stats", e);
            e.printStackTrace();
        }


    }

    private String getMetricName(String metric) {
        if (metric == null) {
            return null;
        }
        String metricName = metric.substring(0, metric.indexOf("{") > 0 ? metric.indexOf("{") : metric.indexOf(" "));
        return metricName.trim();
    }

    private String getServiceName(String metric) {
        if (metric == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(metric);
        if (!matcher.matches() || matcher.group(1).trim().equals("")) {
            return null;
        }
        return "/" + matcher.group(1).replaceAll("_", "/");
    }

    private Double getMetricValue(String metric) {
        if (metric == null) {
            return null;
        }
        try {
            return Double.parseDouble(metric.substring(metric.indexOf("}") > 0 ? metric.indexOf("}") + 1 : metric.indexOf(" ") + 1).trim());
        } catch (NumberFormatException e) {
            LOG.error("Error in parsing metric value: " + metric, e);
            return null;
        }
    }

    public Map<String, F5Stats> getF5StatsMap() {
        return f5StatsMap;
    }

    class F5Stats {
        int activeNodes=2;
        LinkedList<Double> samples = new LinkedList();
        long samplesSinceLastScale = 0;
        MarathonSDService.ServiceConfig serviceConfig;

        void addTotalRequest(Double totalRequest) {
            if (samples.size() > serviceConfig.noOfSamples) {
                samples.removeFirst();
            }
            samples.add(totalRequest);
            samplesSinceLastScale++;
        }

        public List<Double> rps() {
            List<Double> rps = new ArrayList<>();
            Double lastSample = null;
            for (Double sample : samples) {
                if (lastSample != null) {
                    rps.add((sample - lastSample) / props.getService().getSchedulingIntervalInSecs());
                }
                lastSample = sample;
            }

            return rps;
        }
        public Double overallAverageRps() {
            return (samples.getLast() - samples.getFirst()) / (props.getService().getSchedulingIntervalInSecs() * (samples.size() - 1));
        }

        @Override
        public String toString() {
            return "F5Stats{" +
                    "activeNodes=" + activeNodes +
                    ", samples=" + samples +
                    ", samplesSinceLastScale=" + samplesSinceLastScale +
                    ", serviceConfig=" + serviceConfig +
                    '}';
        }
    }
}
