package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class PythonClientConfig {

    @Bean
    public RestClient pythonRestClient(
            @Value("${python.service.url}") String baseUrl,
            @Value("${python.service.connect-timeout:2s}") Duration connectTimeout,
            @Value("${python.service.read-timeout:15s}") Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
