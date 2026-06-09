package com.peti.backend.service.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.peti.backend.dto.stats.PlatformStatisticsDto;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.repository.CaretakerRepository;
import com.peti.backend.repository.CityRepository;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlatformStatisticsServiceTest {

  @Mock
  private ElasticSlotRepository elasticSlotRepository;

  @Mock
  private CityRepository cityRepository;

  @Mock
  private CaretakerRepository caretakerRepository;

  @InjectMocks
  private PlatformStatisticsService service;

  @Test
  @DisplayName("regenerate collects all statistics correctly")
  void regenerateCollectsAllStatistics() {
    when(elasticSlotRepository.count()).thenReturn(100L);
    when(cityRepository.count()).thenReturn(5L);
    when(caretakerRepository.count()).thenReturn(20L);
    when(elasticSlotRepository.countByServiceType("WALKING")).thenReturn(40L);
    when(elasticSlotRepository.countByServiceType("SITTING")).thenReturn(30L);
    when(elasticSlotRepository.countByServiceType("TRAINING")).thenReturn(15L);
    when(elasticSlotRepository.countByServiceType("GROOMING")).thenReturn(10L);
    when(elasticSlotRepository.countByServiceType("VET")).thenReturn(5L);

    PlatformStatisticsDto result = service.regenerate();

    assertThat(result.totalSlotsCount()).isEqualTo(100L);
    assertThat(result.citiesCount()).isEqualTo(5L);
    assertThat(result.caretakersCount()).isEqualTo(20L);
    assertThat(result.slotsPerCategory()).containsEntry(ServiceType.WALKING, 40L);
    assertThat(result.slotsPerCategory()).containsEntry(ServiceType.SITTING, 30L);
    assertThat(result.categories()).doesNotContain(ServiceType.UNDEFINED);
    assertThat(result.generatedAt()).isNotNull();
  }

  @Test
  @DisplayName("getStatistics returns cached value after regenerate")
  void getStatisticsReturnsCachedValue() {
    when(elasticSlotRepository.count()).thenReturn(50L);
    when(cityRepository.count()).thenReturn(3L);
    when(caretakerRepository.count()).thenReturn(10L);
    when(elasticSlotRepository.countByServiceType("WALKING")).thenReturn(20L);
    when(elasticSlotRepository.countByServiceType("SITTING")).thenReturn(10L);
    when(elasticSlotRepository.countByServiceType("TRAINING")).thenReturn(10L);
    when(elasticSlotRepository.countByServiceType("GROOMING")).thenReturn(5L);
    when(elasticSlotRepository.countByServiceType("VET")).thenReturn(5L);

    service.regenerate();
    PlatformStatisticsDto cached = service.getStatistics();

    assertThat(cached.totalSlotsCount()).isEqualTo(50L);
  }

  @Test
  @DisplayName("getStatistics triggers regenerate when cache is empty")
  void getStatisticsTriggersRegenerateWhenEmpty() {
    when(elasticSlotRepository.count()).thenReturn(10L);
    when(cityRepository.count()).thenReturn(1L);
    when(caretakerRepository.count()).thenReturn(2L);
    when(elasticSlotRepository.countByServiceType("WALKING")).thenReturn(5L);
    when(elasticSlotRepository.countByServiceType("SITTING")).thenReturn(3L);
    when(elasticSlotRepository.countByServiceType("TRAINING")).thenReturn(1L);
    when(elasticSlotRepository.countByServiceType("GROOMING")).thenReturn(1L);
    when(elasticSlotRepository.countByServiceType("VET")).thenReturn(0L);

    PlatformStatisticsDto result = service.getStatistics();

    assertThat(result).isNotNull();
    assertThat(result.totalSlotsCount()).isEqualTo(10L);
  }
}

