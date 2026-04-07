package com.peti.backend.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.peti.backend.service.SlotGenerationScheduler;
import com.peti.backend.service.SlotGenerationScheduler.SlotGenerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for AdminSlotController.
 */
class AdminSlotControllerTest {

  private SlotGenerationScheduler slotGenerationScheduler;
  private AdminSlotController controller;

  @BeforeEach
  void setUp() {
    slotGenerationScheduler = mock(SlotGenerationScheduler.class);
    controller = new AdminSlotController(slotGenerationScheduler);
  }

  @Test
  void testGenerateSlots_Success_ReturnsResult() {
    // Given
    SlotGenerationResult expectedResult = new SlotGenerationResult(10, 150, 0);
    when(slotGenerationScheduler.generateSlots()).thenReturn(expectedResult);

    // When
    ResponseEntity<SlotGenerationResult> response = controller.generateSlots();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(10, response.getBody().rruleProcessed());
    assertEquals(150, response.getBody().slotsCreated());
    assertEquals(0, response.getBody().errors());

    verify(slotGenerationScheduler, times(1)).generateSlots();
  }

  @Test
  void testGenerateSlots_NoSlotsCreated_ReturnsResult() {
    // Given: No slots were created
    SlotGenerationResult expectedResult = new SlotGenerationResult(5, 0, 0);
    when(slotGenerationScheduler.generateSlots()).thenReturn(expectedResult);

    // When
    ResponseEntity<SlotGenerationResult> response = controller.generateSlots();

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().slotsCreated());
  }

  @Test
  void testGenerateSlots_WithErrors_ReturnsResultWithErrors() {
    // Given: Some errors occurred during generation
    SlotGenerationResult expectedResult = new SlotGenerationResult(10, 120, 3);
    when(slotGenerationScheduler.generateSlots()).thenReturn(expectedResult);

    // When
    ResponseEntity<SlotGenerationResult> response = controller.generateSlots();

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(3, response.getBody().errors());
    assertEquals(120, response.getBody().slotsCreated());
  }
}

