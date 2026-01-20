package com.peti.backend.dto.event;

import java.util.List;
import java.util.UUID;

public record RequestEventDto(List<UUID> slotsIds,
                              List<UUID> petsIds) {

}
