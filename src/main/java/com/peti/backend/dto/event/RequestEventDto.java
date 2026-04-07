package com.peti.backend.dto.event;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record RequestEventDto(
    @NotEmpty(message = "Slots list must not be empty")
    List<UUID> slotsIds,

    @NotEmpty(message = "Pets list must not be empty")
    List<UUID> petsIds) {

}
