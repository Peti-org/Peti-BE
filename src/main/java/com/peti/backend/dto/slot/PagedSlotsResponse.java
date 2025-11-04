package com.peti.backend.dto.slot;

import java.util.List;

public record PagedSlotsResponse(List<SlotDto> slots, SlotCursor nextCursor) {

}
