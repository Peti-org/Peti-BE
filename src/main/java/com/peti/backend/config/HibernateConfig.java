package com.peti.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
public class HibernateConfig implements HibernatePropertiesCustomizer {

  private final ObjectMapper objectMapper;

  public HibernateConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void customize(Map<String, Object> hibernateProperties) {
    hibernateProperties.put(
        "hibernate.type.json_format_mapper",
        new JacksonJsonFormatMapper(objectMapper)
    );
  }
}
