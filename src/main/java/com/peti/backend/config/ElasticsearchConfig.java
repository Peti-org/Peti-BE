package com.peti.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

  @Value("${spring.elasticsearch.uri:localhost:9200}")
  private String elasticsearchUri;

  @Value("${spring.elasticsearch.connection-timeout:5000}")
  private long connectionTimeout;

  @Value("${spring.elasticsearch.socket-timeout:30000}")
  private long socketTimeout;

  @Override
  public ClientConfiguration clientConfiguration() {
    return ClientConfiguration.builder()
        .connectedTo(elasticsearchUri)
        .withConnectTimeout(connectionTimeout)
        .withSocketTimeout(socketTimeout)
        .build();
  }
}
