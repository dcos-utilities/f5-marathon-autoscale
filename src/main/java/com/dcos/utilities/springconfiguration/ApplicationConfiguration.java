package com.dcos.utilities.springconfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

@Configuration
public class ApplicationConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Bean
    public Filter loggingFilter() {
        AbstractRequestLoggingFilter filter = new AbstractRequestLoggingFilter() {

            @Override
            protected void beforeRequest(HttpServletRequest request, String message) {
            }

            @Override
            protected void afterRequest(HttpServletRequest request, String message) {
                if (request.getRequestURI().contains("/health") || request.getRequestURI().contains("/metrics")) {
                    LOG.trace("Request: {}", message);
                } else {
                    LOG.info("Request: {}", message);
                }
            }
        };
        filter.setIncludeClientInfo(true);
        filter.setIncludePayload(true);
        filter.setIncludeQueryString(true);
        filter.setMaxPayloadLength(99999);
        filter.setAfterMessagePrefix("");

        return filter;
    }
}
