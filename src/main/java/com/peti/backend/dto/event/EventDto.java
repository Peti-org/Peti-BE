package com.peti.backend.dto.event;

import com.peti.backend.dto.PriceDto;
import java.util.UUID;

public record EventDto(UUID eventId,
                       UUID caretakerId,
                       UUID userId,
                       PriceDto price,
                       String status) {
}
