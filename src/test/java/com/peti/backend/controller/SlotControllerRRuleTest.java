package com.peti.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.service.CaretakerRRuleService;
import com.peti.backend.service.CaretakerService;
import com.peti.backend.service.SlotService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SlotControllerRRuleTest {

  @Mock
  private SlotService slotService;

  @Mock
  private CaretakerService caretakerService;

  @Mock
  private CaretakerRRuleService rruleService;

  @InjectMocks
  private SlotController slotController;

  @Test
  void testGetAllRRules_Success() {
    // Given
    UUID caretakerId = UUID.fromString("c1d2e3f4-a5b6-7890-1234-567890abcdef");
    RRuleDto rruleDto1 = ResourceLoader.loadResource("rrule-response.json", RRuleDto.class);
    RRuleDto rruleDto2 = new RRuleDto(
        UUID.fromString("22222222-2222-2222-2222-222222222222"),
        "FREQ=WEEKLY;BYDAY=MO,WE,FR",
        rruleDto1.dtstart(),
        rruleDto1.dtend(),
        "Evening availability",
        "walk"
    );

    when(rruleService.getAllRRulesForCaretaker(caretakerId)).thenReturn(List.of(rruleDto1, rruleDto2));

    // When
    ResponseEntity<List<RRuleDto>> response = slotController.getAllRRules(caretakerId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());
    assertEquals(rruleDto1.rruleId(), response.getBody().get(0).rruleId());
    assertEquals(rruleDto2.rruleId(), response.getBody().get(1).rruleId());
    verify(rruleService).getAllRRulesForCaretaker(caretakerId);
  }

  @Test
  void testGetAllRRules_EmptyList() {
    // Given
    UUID caretakerId = UUID.randomUUID();
    when(rruleService.getAllRRulesForCaretaker(caretakerId)).thenReturn(List.of());

    // When
    ResponseEntity<List<RRuleDto>> response = slotController.getAllRRules(caretakerId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().size());
    verify(rruleService).getAllRRulesForCaretaker(caretakerId);
  }

  @Test
  void testCreateRRule_Success() {
    // Given
    UUID caretakerId = UUID.fromString("c1d2e3f4-a5b6-7890-1234-567890abcdef");
    RequestRRuleDto createDto = ResourceLoader.loadResource("rrule-create-request.json", RequestRRuleDto.class);
    RRuleDto responseDto = ResourceLoader.loadResource("rrule-response.json", RRuleDto.class);

    when(rruleService.createRRule(any(RequestRRuleDto.class), eq(caretakerId))).thenReturn(responseDto);

    // When
    ResponseEntity<RRuleDto> response = slotController.createRRule(createDto, caretakerId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(responseDto.rruleId(), response.getBody().rruleId());
    assertEquals(responseDto.rrule(), response.getBody().rrule());
    assertEquals(responseDto.description(), response.getBody().description());
    verify(rruleService).createRRule(createDto, caretakerId);
  }

  @Test
  void testUpdateRRule_Success() {
    // Given
    UUID caretakerId = UUID.fromString("c1d2e3f4-a5b6-7890-1234-567890abcdef");
    UUID rruleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    RequestRRuleDto updateDto = ResourceLoader.loadResource("rrule-update-request.json", RequestRRuleDto.class);
    RRuleDto updatedDto = new RRuleDto(
        rruleId,
        updateDto.rrule(),
        updateDto.dtstart(),
        updateDto.dtend(),
        updateDto.description(),
        updateDto.slotType());

    when(rruleService.updateRRule(eq(rruleId), any(RequestRRuleDto.class), eq(caretakerId)))
        .thenReturn(Optional.of(updatedDto));

    // When
    ResponseEntity<RRuleDto> response = slotController.updateRRule(rruleId, updateDto, caretakerId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(rruleId, response.getBody().rruleId());
    assertEquals(updateDto.rrule(), response.getBody().rrule());
    assertEquals(updateDto.description(), response.getBody().description());
    verify(rruleService).updateRRule(rruleId, updateDto, caretakerId);
  }

  @Test
  void testUpdateRRule_NotFound() {
    // Given
    UUID caretakerId = UUID.randomUUID();
    UUID rruleId = UUID.randomUUID();
    RequestRRuleDto updateDto = ResourceLoader.loadResource("rrule-update-request.json", RequestRRuleDto.class);

    when(rruleService.updateRRule(eq(rruleId), any(RequestRRuleDto.class), eq(caretakerId)))
        .thenReturn(Optional.empty());

    // When
    ResponseEntity<RRuleDto> response = slotController.updateRRule(rruleId, updateDto, caretakerId);

    // Then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(rruleService).updateRRule(rruleId, updateDto, caretakerId);
  }

  @Test
  void testUpdateRRule_WrongCaretaker() {
    // Given
    UUID caretakerId = UUID.randomUUID();
    UUID wrongCaretakerId = UUID.randomUUID();
    UUID rruleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    RequestRRuleDto updateDto = ResourceLoader.loadResource("rrule-update-request.json", RequestRRuleDto.class);

    when(rruleService.updateRRule(eq(rruleId), any(RequestRRuleDto.class), eq(wrongCaretakerId)))
        .thenReturn(Optional.empty());

    // When
    ResponseEntity<RRuleDto> response = slotController.updateRRule(rruleId, updateDto, wrongCaretakerId);

    // Then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(rruleService).updateRRule(rruleId, updateDto, wrongCaretakerId);
  }

  @Test
  void testDeleteRRule_Success() {
    // Given
    UUID caretakerId = UUID.fromString("c1d2e3f4-a5b6-7890-1234-567890abcdef");
    UUID rruleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    RRuleDto deletedDto = ResourceLoader.loadResource("rrule-response.json", RRuleDto.class);

    when(rruleService.deleteRRule(rruleId, caretakerId)).thenReturn(Optional.of(deletedDto));

    // When
    ResponseEntity<RRuleDto> response = slotController.deleteRRule(rruleId, caretakerId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(rruleId, response.getBody().rruleId());
    verify(rruleService).deleteRRule(rruleId, caretakerId);
  }

  @Test
  void testDeleteRRule_NotFound() {
    // Given
    UUID caretakerId = UUID.randomUUID();
    UUID rruleId = UUID.randomUUID();

    when(rruleService.deleteRRule(rruleId, caretakerId)).thenReturn(Optional.empty());

    // When
    ResponseEntity<RRuleDto> response = slotController.deleteRRule(rruleId, caretakerId);

    // Then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(rruleService).deleteRRule(rruleId, caretakerId);
  }

  @Test
  void testDeleteRRule_WrongCaretaker() {
    // Given
    UUID wrongCaretakerId = UUID.randomUUID();
    UUID rruleId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    when(rruleService.deleteRRule(rruleId, wrongCaretakerId)).thenReturn(Optional.empty());

    // When
    ResponseEntity<RRuleDto> response = slotController.deleteRRule(rruleId, wrongCaretakerId);

    // Then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(rruleService).deleteRRule(rruleId, wrongCaretakerId);
  }
}

