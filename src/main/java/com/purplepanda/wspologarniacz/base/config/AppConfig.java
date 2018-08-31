package com.purplepanda.wspologarniacz.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    String secretKey(@Value("${security.jwts.secret}") String secretKey){
        return secretKey;
    }
}
