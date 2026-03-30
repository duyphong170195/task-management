package com.eight_seneca.task_management.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@AutoConfiguration
@Configuration
public class ConfigurationBean {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(false);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

}
