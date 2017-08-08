package com.dcos.utilities.springconfiguration.properties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Marathon {
    @Valid
    @NotNull
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public static final class MarathonBuilder {
        private String url;

        private MarathonBuilder() {
        }

        public static MarathonBuilder builder() {
            return new MarathonBuilder();
        }

        public MarathonBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Marathon build() {
            Marathon marathon = new Marathon();
            marathon.setUrl(url);
            return marathon;
        }
    }
}
