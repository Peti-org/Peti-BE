package com.peti.backend.service.slot;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * CRUD operations for Elasticsearch slot documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlotCrudService {

  private final ElasticSlotRepository slotRepository;


  public List<ElasticSlotDocument> replaceSlotsByCaretakerAndDate(
      String caretakerId, LocalDate date, List<ElasticSlotDocument> newSlots) {
    log.info("Replacing slots for caretaker {} on date {} with {} new slots",
        caretakerId, date, newSlots.size());
    deleteSlotsByCaretakerAndDate(caretakerId, date);
    return bulkSaveSlots(newSlots);
  }


  // not used in production for now
  public List<ElasticSlotDocument> getAllSlots() {
    return StreamSupport.stream(slotRepository.findAll().spliterator(), false).toList();
  }

  public Optional<ElasticSlotDocument> getSlotById(String id) {
    return slotRepository.findById(id);
  }

  public List<ElasticSlotDocument> getSlotsByCaretaker(String caretakerId) {
    return slotRepository.findByCaretakerIdAndFromDateTimeBetween(
        caretakerId, LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(30).atStartOfDay());
  }

  public ElasticSlotDocument saveSlot(ElasticSlotDocument slot) {
    return slotRepository.save(slot);
  }

  public void deleteSlot(String id) {
    slotRepository.deleteById(id);
  }

  public void deleteSlotsByCaretakerAndDate(String caretakerId, LocalDate date) {
    log.info("Deleting all slots for caretaker {} on date {}", caretakerId, date);
    slotRepository.deleteByCaretakerIdAndFromDateTimeBetween(
        caretakerId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
  }

  public List<ElasticSlotDocument> bulkSaveSlots(List<ElasticSlotDocument> slots) {
    log.info("Bulk saving {} slots", slots.size());
    return StreamSupport.stream(slotRepository.saveAll(slots).spliterator(), false).toList();
  }

  public long countSlots() {
    return slotRepository.count();
  }
}

