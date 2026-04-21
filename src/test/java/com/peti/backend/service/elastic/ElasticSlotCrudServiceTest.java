package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticSlotCrudServiceTest {

  @Mock
  private ElasticSlotRepository slotRepository;

  @InjectMocks
  private ElasticSlotCrudService crudService;

  @Test
  @DisplayName("getSlotById - found")
  void getSlotById_found() {
    ElasticSlotDocument doc = buildSlot("slot-1");
    when(slotRepository.findById("slot-1")).thenReturn(Optional.of(doc));

    Optional<ElasticSlotDocument> result = crudService.getSlotById("slot-1");
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo("slot-1");
  }

  @Test
  @DisplayName("getSlotById - not found")
  void getSlotById_notFound() {
    when(slotRepository.findById("missing")).thenReturn(Optional.empty());
    assertThat(crudService.getSlotById("missing")).isEmpty();
  }

  @Test
  @DisplayName("saveSlot delegates to repository")
  void saveSlot() {
    ElasticSlotDocument doc = buildSlot("slot-1");
    when(slotRepository.save(doc)).thenReturn(doc);

    ElasticSlotDocument result = crudService.saveSlot(doc);
    assertThat(result.getId()).isEqualTo("slot-1");
    verify(slotRepository).save(doc);
  }

  @Test
  @DisplayName("deleteSlot delegates to repository")
  void deleteSlot() {
    crudService.deleteSlot("slot-1");
    verify(slotRepository).deleteById("slot-1");
  }

  @Test
  @DisplayName("deleteSlotsByCaretakerAndDate delegates to repository")
  void deleteByCaretakerAndDate() {
    LocalDate date = LocalDate.of(2026, 3, 1);
    crudService.deleteSlotsByCaretakerAndDate("ct-1", date);
    verify(slotRepository).deleteByCaretakerIdAndDate("ct-1", date);
  }

  @Test
  @DisplayName("bulkSaveSlots saves all")
  void bulkSaveSlots() {
    List<ElasticSlotDocument> docs = List.of(buildSlot("s1"), buildSlot("s2"));
    when(slotRepository.saveAll(docs)).thenReturn(docs);

    List<ElasticSlotDocument> result = crudService.bulkSaveSlots(docs);
    assertThat(result).hasSize(2);
    verify(slotRepository).saveAll(docs);
  }

  @Test
  @DisplayName("replaceSlotsByCaretakerAndDate deletes then saves")
  void replaceSlots() {
    LocalDate date = LocalDate.of(2026, 3, 1);
    List<ElasticSlotDocument> newSlots = List.of(buildSlot("new-1"));
    when(slotRepository.saveAll(newSlots)).thenReturn(newSlots);

    List<ElasticSlotDocument> result = crudService.replaceSlotsByCaretakerAndDate("ct-1", date, newSlots);
    verify(slotRepository).deleteByCaretakerIdAndDate("ct-1", date);
    verify(slotRepository).saveAll(newSlots);
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("countSlots delegates to repository")
  void countSlots() {
    when(slotRepository.count()).thenReturn(42L);
    assertThat(crudService.countSlots()).isEqualTo(42L);
  }

  private ElasticSlotDocument buildSlot(String id) {
    return ElasticSlotDocument.builder()
        .id(id)
        .caretakerId("ct-1")
        .date(LocalDate.of(2026, 3, 1))
        .timeFrom(LocalTime.of(8, 0))
        .timeTo(LocalTime.of(12, 0))
        .capacity(3)
        .build();
  }
}

