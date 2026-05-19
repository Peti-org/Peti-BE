package com.peti.backend.repository.elastic;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticSlotRepository extends ElasticsearchRepository<ElasticSlotDocument, String> {

  /**
   * Find all slots for a caretaker within a date-time range.
   */
  List<ElasticSlotDocument> findByCaretakerIdAndFromDateTimeBetween(String caretakerId, LocalDateTime from, LocalDateTime to);

  /**
   * Find slots within a date-time range.
   */
  List<ElasticSlotDocument> findByFromDateTimeBetween(LocalDateTime from, LocalDateTime to);

  /**
   * Find slots by caretaker's city.
   */
  List<ElasticSlotDocument> findByCaretakerCityId(String cityId);


  /**
   * Delete all slots for a caretaker on a specific date (for regeneration).
   */
  void deleteByCaretakerIdAndFromDateTimeBetween(String caretakerId, LocalDateTime from, LocalDateTime to);

  /**
   * Count slots for a caretaker.
   */
  long countByCaretakerId(String caretakerId);
}
