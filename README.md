# f5-marathon-autoscale-service

A service which talks to f5exporter <code>/metrics</code> and Marathon <code>/v2/apps</code> URLs and makes a decision of whether to increase or decrease the number of DCOS/Marathon service instances based on the requests per second load on the VIP.
Autoscale configuration is provided by using service labels on the DCOS/Marathon applications.

![Architecture](https://github.com/dcos-utilities/f5-marathon-autoscale/blob/master/diagrams/Autoscaling.png)

## Service Configration

The configuration options which can be used to start the service. These settings will be applicable across all the applications which are autoscalable. The default values are best in case the f5exporter application and this application, both are hosted on the same DCOS as per above diagram.

| Environment Variable Name    | Default Value    | Description    |
|:----------------------------:|:----------------:|:--------------:|
| marathon_url | http://leader.mesos:8080 | Marathon URL which the service points to, it has to a path till /v2/apps (excluded) |
| f5Exporter_url | http://f5exporter.marathon.mesos:9142/metrics | The URL to f5 scrapper service's /metrics URL. For more details, look at [f5exporter](https://github.com/ExpressenAB/bigip_exporter) service |
| service_schedulingIntervalInSecs | 120 | Timer for rescanning both, mesos and f5exporter services for new data |
| log_level | debug | Controls the ROOT(Console) logger level for printing |

## Service Level Labels

Each service that wants to get autoscaled has to include the below labels in the json file used to deploy the application. Not all the variables are required. AUTOSCALE=true will make the default values listed below applicable for the service, and they can be overridden by specifying the values

| Label Name    | Value Type    | Default Value    | Description |
|:-------------:|:-------------:|:-------------:|:--------------:|
| AUTOSCALE   | true/false   | false | Flag to enable/disable autoscaling |
| AUTOSCALE_RPS_PER_NODE     | Any postive integer   | 100 | Target request per second per service instance |
| AUTOSCALE_NO_OF_SAMPLES        | Any postive integer   | 10 | Minimum samples required to take the average rps |
| AUTOSCALE_COOLDOWN_SAMPLES        | Any postive integer   | 30 | Samples to pass on after a successful upscale/downscale |
| AUTOSCALE_MINIMUM_NODES        | Any postive integer   | 1 | Minimum service instances to keep |
| AUTOSCALE_MAXIMUM_NODES   | Any postive integer   | 10 | Maximum service instances to keep |

## Health URLs

* /metrics - Exposes metrics in prometheus format for dependencies health
* /health - Returns 200 if the service is active

## Dockerhub

https://hub.docker.com/r/dcosutilities/f5-marathon-autoscale/tags/
