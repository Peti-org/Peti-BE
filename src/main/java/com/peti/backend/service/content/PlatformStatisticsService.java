package com.peti.backend.service.content;

import com.peti.backend.dto.stats.PlatformStatisticsDto;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.repository.CaretakerRepository;
import com.peti.backend.repository.CityRepository;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformStatisticsService {

  private static final List<ServiceType> ACTIVE_CATEGORIES = Arrays.stream(ServiceType.values())
      .filter(type -> type != ServiceType.UNDEFINED)
      .toList();

  private final ElasticSlotRepository elasticSlotRepository;
  private final CityRepository cityRepository;
  private final CaretakerRepository caretakerRepository;

  private final AtomicReference<PlatformStatisticsDto> cachedStats = new AtomicReference<>();

  public PlatformStatisticsDto getStatistics() {
    PlatformStatisticsDto stats = cachedStats.get();
    if (stats == null) {
      stats = regenerate();
    }
    return stats;
  }

  public PlatformStatisticsDto regenerate() {
    log.info("Regenerating platform statistics");
    long totalSlots = elasticSlotRepository.count();
    long citiesCount = cityRepository.count();
    long caretakersCount = caretakerRepository.count();

    Map<ServiceType, Long> slotsPerCategory = new EnumMap<>(ServiceType.class);
    for (ServiceType type : ACTIVE_CATEGORIES) {
      long count = elasticSlotRepository.countByServiceType(type.name());
      slotsPerCategory.put(type, count);
    }

    PlatformStatisticsDto stats = new PlatformStatisticsDto(
        ACTIVE_CATEGORIES,
        citiesCount,
        totalSlots,
        slotsPerCategory,
        caretakersCount,
        LocalDateTime.now()
    );

    cachedStats.set(stats);
    log.info("Platform statistics regenerated: {} total slots, {} cities, {} caretakers",
        totalSlots, citiesCount, caretakersCount);
    return stats;
  }

  @Scheduled(cron = "0 0 1 * * *")
  public void scheduledRegenerate() {
    regenerate();
  }
}

