package com.peti.backend.service.slot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.dto.slot.SlotGenerationMode;
import com.peti.backend.dto.slot.SlotGenerationRequest;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.repository.CaretakerRepository;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlotGenerationSchedulerTest {

  @Mock
  private SlotsRebuildTrigger slotsRebuildTrigger;
  @Mock
  private CaretakerRepository caretakerRepository;

  @InjectMocks
  private SlotGenerationScheduler scheduler;

  private Caretaker caretaker;

  @BeforeEach
  void setUp() throws Exception {
    caretaker = new Caretaker();
    caretaker.setCaretakerId(UUID.randomUUID());

    Field daysAheadField = SlotGenerationScheduler.class.getDeclaredField("daysAhead");
    daysAheadField.setAccessible(true);
    daysAheadField.setInt(scheduler, 60);
  }

  @Test
  void defaultMode_skipsAlreadyGenerated() {
    caretaker.setGeneratedTo(LocalDate.now().plusDays(60));
    when(caretakerRepository.findAll()).thenReturn(List.of(caretaker));

    var request = new SlotGenerationRequest(SlotGenerationMode.DEFAULT, null, null, null);
    var result = scheduler.generateSlots(request);

    assertEquals(1, result.caretakersProcessed());
    verify(slotsRebuildTrigger, never()).rebuild(any(), any(), any());
  }

  @Test
  void defaultMode_generatesFromGeneratedToOnwards() {
    LocalDate generatedTo = LocalDate.now().minusDays(1);
    caretaker.setGeneratedTo(generatedTo);
    when(caretakerRepository.findAll()).thenReturn(List.of(caretaker));
    when(caretakerRepository.save(caretaker)).thenReturn(caretaker);

    var request = new SlotGenerationRequest(SlotGenerationMode.DEFAULT, null, null, null);
    var result = scheduler.generateSlots(request);

    assertEquals(1, result.caretakersProcessed());
    verify(slotsRebuildTrigger).rebuild(
        eq(caretaker.getCaretakerId()),
        eq(generatedTo.plusDays(1)),
        eq(LocalDate.now().plusDays(60)));
  }

  @Test
  void forceMode_regeneratesFullRange() {
    caretaker.setGeneratedTo(LocalDate.now().plusDays(60));
    when(caretakerRepository.findAll()).thenReturn(List.of(caretaker));
    when(caretakerRepository.save(caretaker)).thenReturn(caretaker);

    var request = new SlotGenerationRequest(SlotGenerationMode.FORCE, null, null, null);
    var result = scheduler.generateSlots(request);

    assertEquals(1, result.caretakersProcessed());
    verify(slotsRebuildTrigger).rebuild(
        eq(caretaker.getCaretakerId()),
        eq(LocalDate.now()),
        eq(LocalDate.now().plusDays(60)));
  }

  @Test
  void specificCaretaker_onlyProcessesThatOne() {
    UUID id = caretaker.getCaretakerId();
    when(caretakerRepository.findById(id)).thenReturn(Optional.of(caretaker));
    when(caretakerRepository.save(caretaker)).thenReturn(caretaker);

    var request = new SlotGenerationRequest(SlotGenerationMode.FORCE, null, null, id);
    var result = scheduler.generateSlots(request);

    assertEquals(1, result.caretakersProcessed());
    verify(caretakerRepository, never()).findAll();
  }

  @Test
  void errorHandling_countsErrors() {
    when(caretakerRepository.findAll()).thenReturn(List.of(caretaker));
    doThrow(new RuntimeException("test")).when(slotsRebuildTrigger)
        .rebuild(any(), any(), any());

    var request = new SlotGenerationRequest(SlotGenerationMode.FORCE, null, null, null);
    var result = scheduler.generateSlots(request);

    assertEquals(0, result.caretakersProcessed());
    assertEquals(1, result.errors());
  }
}

