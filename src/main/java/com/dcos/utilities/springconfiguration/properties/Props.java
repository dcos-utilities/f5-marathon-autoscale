package com.dcos.utilities.springconfiguration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties
public class Props {

    @Valid
    @NotNull
    private Marathon marathon;

    @Valid
    @NotNull
    private Service service;

    @Valid
    @NotNull
    private F5Exporter f5Exporter;


    public Marathon getMarathon() {
        return marathon;
    }

    public void setMarathon(Marathon marathon) {
        this.marathon = marathon;
    }

    public F5Exporter getF5Exporter() {
        return f5Exporter;
    }

    public void setF5Exporter(F5Exporter f5Exporter) {
        this.f5Exporter = f5Exporter;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }


    public static final class PropsBuilder {
        private Marathon marathon;
        private Service service;
        private F5Exporter f5Exporter;

        private PropsBuilder() {
        }

        public static PropsBuilder builder() {
            return new PropsBuilder();
        }

        public PropsBuilder withMarathon(Marathon marathon) {
            this.marathon = marathon;
            return this;
        }

        public PropsBuilder withService(Service service) {
            this.service = service;
            return this;
        }

        public PropsBuilder withF5Exporter(F5Exporter f5Exporter) {
            this.f5Exporter = f5Exporter;
            return this;
        }

        public Props build() {
            Props props = new Props();
            props.setMarathon(marathon);
            props.setService(service);
            props.setF5Exporter(f5Exporter);
            return props;
        }
    }
}
