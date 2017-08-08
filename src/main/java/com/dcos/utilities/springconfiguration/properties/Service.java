package com.dcos.utilities.springconfiguration.properties;

public class Service {
    private String version;

    private Integer schedulingIntervalInSecs;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getSchedulingIntervalInSecs() {
        return schedulingIntervalInSecs;
    }

    public void setSchedulingIntervalInSecs(Integer schedulingIntervalInSecs) {
        this.schedulingIntervalInSecs = schedulingIntervalInSecs;
    }

    @Override
    public String toString() {
        return "Service{" +
                "version='" + version + '\'' +
                ", schedulingIntervalInSecs=" + schedulingIntervalInSecs +
                '}';
    }


    public static final class ServiceBuilder {
        private String version;
        private Integer schedulingIntervalInSecs;

        private ServiceBuilder() {
        }

        public static ServiceBuilder builder() {
            return new ServiceBuilder();
        }

        public ServiceBuilder withVersion(String version) {
            this.version = version;
            return this;
        }

        public ServiceBuilder withSchedulingIntervalInSecs(Integer schedulingIntervalInSecs) {
            this.schedulingIntervalInSecs = schedulingIntervalInSecs;
            return this;
        }

        public Service build() {
            Service service = new Service();
            service.setVersion(version);
            service.setSchedulingIntervalInSecs(schedulingIntervalInSecs);
            return service;
        }
    }
}
