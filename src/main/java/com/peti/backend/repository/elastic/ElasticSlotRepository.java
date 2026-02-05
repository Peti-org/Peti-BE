package com.peti.backend.repository.elastic;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticSlotRepository extends ElasticsearchRepository<ElasticSlotDocument, String> {

  /**
   * Find all slots for a caretaker on a specific date.
   */
  List<ElasticSlotDocument> findByCaretakerIdAndDate(String caretakerId, LocalDate date);

  /**
   * Find all slots for a caretaker within a date range.
   */
  List<ElasticSlotDocument> findByCaretakerIdAndDateBetween(String caretakerId, LocalDate startDate, LocalDate endDate);

  /**
   * Find slots by date.
   */
  List<ElasticSlotDocument> findByDate(LocalDate date);

  /**
   * Find slots within date range.
   */
  List<ElasticSlotDocument> findByDateBetween(LocalDate startDate, LocalDate endDate);



  /**
   * Find slots by caretaker's city.
   */
  List<ElasticSlotDocument> findByCaretakerCityId(String cityId);

  /**
   * Find slots that overlap with a time range on a specific date.
   */
  List<ElasticSlotDocument> findByCaretakerIdAndDateAndTimeFromLessThanEqualAndTimeToGreaterThanEqual(
      String caretakerId, LocalDate date, LocalTime timeTo, LocalTime timeFrom);

  /**
   * Delete all slots for a caretaker on a specific date (for regeneration).
   */
  void deleteByCaretakerIdAndDate(String caretakerId, LocalDate date);

  /**
   * Count slots for a caretaker.
   */
  long countByCaretakerId(String caretakerId);

  /**
   * Find slots by RRULE ID.
   */
  List<ElasticSlotDocument> findByRruleId(String rruleId);
}
