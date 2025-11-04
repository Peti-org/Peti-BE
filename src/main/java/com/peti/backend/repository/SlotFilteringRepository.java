package com.peti.backend.repository;

import com.peti.backend.dto.slot.SlotCursor;
import com.peti.backend.dto.slot.SlotFiltersDto;
import com.peti.backend.model.domain.Slot;
import java.util.List;

public interface SlotFilteringRepository {

  List<Slot> findSlotsWithCursor(SlotFiltersDto filter, SlotCursor cursor);

}
