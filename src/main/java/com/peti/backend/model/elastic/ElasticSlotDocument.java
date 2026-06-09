package com.peti.backend.model.elastic;

import com.peti.backend.dto.caretaker.ServiceConfig;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "slots")
public class ElasticSlotDocument {

  @Id
  private String id;

  @Version
  private Long version;

  // Caretaker (service provider) information — denormalised for efficient searching
  @Field(type = FieldType.Keyword)
  private String caretakerId;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String caretakerFirstName;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String caretakerLastName;

  @Field(type = FieldType.Integer)
  private Integer caretakerRating;

  @Field(type = FieldType.Keyword)
  private String caretakerCityId;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String caretakerCityName;

  @Field(type = FieldType.Keyword)
  private String serviceType;

  // The specific service config that is active for this slot (stored as opaque JSON)
  @Field(type = FieldType.Object, enabled = false)
  private ServiceConfig serviceConfig;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime fromDateTime;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime toDateTime;

  @Field(type = FieldType.Integer)
  private Integer capacity;

  // Metadata
  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private Instant createdAt;
}
