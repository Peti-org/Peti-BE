package com.peti.backend.dto.stats;

import com.peti.backend.model.internal.ServiceType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record PlatformStatisticsDto(
    List<ServiceType> categories,
    long citiesCount,
    long totalSlotsCount,
    Map<ServiceType, Long> slotsPerCategory,
    long caretakersCount,
    LocalDateTime generatedAt
) {}

