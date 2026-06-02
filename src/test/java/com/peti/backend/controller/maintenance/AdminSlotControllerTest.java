package com.peti.backend.controller.maintenance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.dto.slot.SlotGenerationMode;
import com.peti.backend.dto.slot.SlotGenerationRequest;
import com.peti.backend.service.slot.SlotGenerationScheduler;
import com.peti.backend.service.slot.SlotGenerationScheduler.SlotGenerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AdminSlotControllerTest {

  private SlotGenerationScheduler slotGenerationScheduler;
  private AdminSlotController controller;

  @BeforeEach
  void setUp() {
    slotGenerationScheduler = mock(SlotGenerationScheduler.class);
    controller = new AdminSlotController(slotGenerationScheduler);
  }

  @Test
  void generateSlots_defaultMode_returnsResult() {
    SlotGenerationRequest request = new SlotGenerationRequest(SlotGenerationMode.DEFAULT, null, null, null);
    SlotGenerationResult expectedResult = new SlotGenerationResult(10, 150, 0);
    when(slotGenerationScheduler.generateSlots(request)).thenReturn(expectedResult);

    ResponseEntity<SlotGenerationResult> response = controller.generateSlots(request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(10, response.getBody().caretakersProcessed());
    assertEquals(0, response.getBody().errors());
    verify(slotGenerationScheduler).generateSlots(request);
  }

  @Test
  void generateSlots_forceMode_returnsResult() {
    SlotGenerationRequest request = new SlotGenerationRequest(SlotGenerationMode.FORCE, null, null, null);
    SlotGenerationResult expectedResult = new SlotGenerationResult(5, 200, 1);
    when(slotGenerationScheduler.generateSlots(request)).thenReturn(expectedResult);

    ResponseEntity<SlotGenerationResult> response = controller.generateSlots(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(5, response.getBody().caretakersProcessed());
    assertEquals(1, response.getBody().errors());
  }
}

