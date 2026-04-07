package com.peti.backend.dto.slot;

import com.peti.backend.dto.caretacker.SimpleCaretakerDto;
import com.peti.backend.model.internal.ServiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SlotDto(UUID slotId,
                      SimpleCaretakerDto caretaker,
                      LocalDate date,
                      LocalTime timeFrom,
                      LocalTime timeTo,
                      ServiceType type,
                      BigDecimal price,
                      String currency,
                      Integer capacity,
                      Boolean isRepeated) {

}
