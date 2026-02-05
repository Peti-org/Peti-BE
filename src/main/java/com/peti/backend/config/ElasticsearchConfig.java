package com.peti.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableElasticsearchRepositories(basePackages = "com.peti.backend.repository.elastic")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

  @Value("${spring.elasticsearch.uris:http://localhost:9200}")
  private String elasticsearchUri;

  @Value("${spring.elasticsearch.connection-timeout:5000}")
  private long connectionTimeout;

  @Value("${spring.elasticsearch.socket-timeout:30000}")
  private long socketTimeout;

  @Override
  public ClientConfiguration clientConfiguration() {
    return ClientConfiguration.builder()
        .connectedTo(elasticsearchUri.replace("http://", "").replace("https://", ""))
        .withConnectTimeout(connectionTimeout)
        .withSocketTimeout(socketTimeout)
        .build();
  }
}
