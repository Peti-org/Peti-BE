package com.peti.backend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AppConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    // Ignore unknown properties during deserialization
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // Ignore null values during serialization
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(Feature.ALLOW_COMMENTS, true);
    // Serialize LocalTime/LocalDate/LocalDateTime as "HH:mm" strings, not arrays
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // Serialize Duration as "PT1H" ISO-8601 string, not seconds number
    objectMapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
    // Serialize BigDecimal as plain number string, e.g. "40.00" not 4E+1
    objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
    objectMapper.findAndRegisterModules();
    return objectMapper;
  }
}
