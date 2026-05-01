package com.peti.backend.controller;

import com.peti.backend.service.user.CaretakerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InputParamsResolver {

  private final CaretakerService caretakerService;


  public UUID resolveCaretakerIdBy(UUID userId) {
    return caretakerService.getCaretakerIdByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Caretaker not found for user: " + userId));
  }


}
