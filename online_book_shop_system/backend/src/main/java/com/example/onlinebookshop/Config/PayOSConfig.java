package com.example.onlinebookshop.Config;

import com.example.onlinebookshop.payos.PayOSProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(PayOSProperties.class)
public class PayOSConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
