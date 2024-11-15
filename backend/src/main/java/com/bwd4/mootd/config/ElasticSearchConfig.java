package com.bwd4.mootd.config;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientOptions;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;

@Configuration
public class ElasticSearchConfig extends ReactiveElasticsearchConfiguration {
    @Value("${spring.data.elasticsearch.username}")
    private String username;

    @Value("${spring.data.elasticsearch.password}")
    private String password;

    @Value("${spring.data.elasticsearch.uris}")
    private String host;


    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(host)
                .withBasicAuth(username,password)
                .build();
    }

    @Bean
    @Primary
    RestClientTransport restClientTransport(RestClient restClient, ObjectProvider<RestClientOptions> restClientOptions) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper(), restClientOptions.getIfAvailable());
    }

}
