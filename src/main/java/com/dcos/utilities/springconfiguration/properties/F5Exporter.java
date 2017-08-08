package com.dcos.utilities.springconfiguration.properties;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class F5Exporter {

    @Valid
    @NotNull
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public static final class F5ExporterBuilder {
        private String url;

        private F5ExporterBuilder() {
        }

        public static F5ExporterBuilder builder() {
            return new F5ExporterBuilder();
        }

        public F5ExporterBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public F5Exporter build() {
            F5Exporter f5Exporter = new F5Exporter();
            f5Exporter.setUrl(url);
            return f5Exporter;
        }
    }
}
