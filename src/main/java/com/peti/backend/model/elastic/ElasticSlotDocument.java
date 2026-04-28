package com.peti.backend.model.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
@Document(indexName = "walking-slots")
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

  // Full caretaker preferences — stored as opaque JSON (not indexed, only used after retrieval)
  @Field(type = FieldType.Object, enabled = false)
  private CaretakerPreferences caretakerPreferences;

  // The specific service config that is active for this slot (stored as opaque JSON)
  @Field(type = FieldType.Object, enabled = false)
  private ServiceConfig serviceConfig;

  // Slot time range
  @Field(type = FieldType.Date, format = DateFormat.date)
  private LocalDate date;

  @Field(type = FieldType.Date, format = DateFormat.hour_minute_second)
  private LocalTime timeFrom;

  @Field(type = FieldType.Date, format = DateFormat.hour_minute_second)
  private LocalTime timeTo;

  // Capacity management
  @Field(type = FieldType.Integer)
  private Integer capacity;

  // Reference to the RRule that generated this slot
  @Field(type = FieldType.Keyword)
  private String rruleId;

  // Metadata
  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private Instant createdAt;

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private Instant updatedAt;

  @Field(type = FieldType.Long)
  private Long sequenceNumber;

  public boolean hasCapacityFor(int petsCount) {
    return capacity != null && capacity >= petsCount;
  }

  public long getDurationMinutes() {
    if (timeFrom == null || timeTo == null) {
      return 0;
    }
    return java.time.Duration.between(timeFrom, timeTo).toMinutes();
  }
}
