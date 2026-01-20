package com.peti.backend.dto;

import com.peti.backend.dto.caretacker.SimpleCaretakerDto;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.dto.user.UserDto;
import java.math.BigDecimal;
import java.util.UUID;

public record EventDto(UUID slotId,
                       SimpleCaretakerDto caretaker,
                       UserDto userDto,
                       SlotDto slotDto,
                       PriceDto price,
                       String status) {

}
