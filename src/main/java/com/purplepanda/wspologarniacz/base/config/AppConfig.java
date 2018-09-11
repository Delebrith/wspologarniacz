package com.purplepanda.wspologarniacz.base.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultHttpLogWriter;
import org.zalando.logbook.Logbook;

import static org.zalando.logbook.Conditions.exclude;
import static org.zalando.logbook.Conditions.requestTo;

@Slf4j
@Configuration
public class AppConfig {
    @Bean
    String secretKey(@Value("${security.jwts.secret}") String secretKey){
        return secretKey;
    }

    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .writer(new DefaultHttpLogWriter(log, DefaultHttpLogWriter.Level.INFO))
                .formatter(new DefaultHttpLogFormatter())
                .condition(exclude(
                        requestTo("/swagger-resources/**"),
                        requestTo("/h2/**"),
                        requestTo("**/swagger-ui/**"),
                        requestTo("/favicon.ico"),
                        requestTo("/public/**"),
                        requestTo("/")
                )).build();
    }
}
