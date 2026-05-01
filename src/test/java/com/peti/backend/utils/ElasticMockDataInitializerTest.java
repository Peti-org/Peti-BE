package com.peti.backend.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.Role;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.repository.CaretakerRepository;
import com.peti.backend.repository.CityRepository;
import com.peti.backend.repository.RoleRepository;
import com.peti.backend.repository.UserRepository;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import com.peti.backend.service.elastic.SlotGenerationService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;

@ExtendWith(MockitoExtension.class)
class ElasticMockDataInitializerTest {

  @Mock
  private ElasticSlotRepository slotRepository;
  @Mock
  private SlotGenerationService slotGenerationService;
  @Mock
  private ElasticsearchOperations elasticsearchOperations;
  @Mock
  private UserRepository userRepository;
  @Mock
  private CaretakerRepository caretakerRepository;
  @Mock
  private CaretakerRRuleRepository rruleRepository;
  @Mock
  private CityRepository cityRepository;
  @Mock
  private RoleRepository roleRepository;
  @Mock
  private IndexOperations indexOperations;

  @InjectMocks
  private ElasticMockDataInitializer initializer;

  @Test
  @DisplayName("initializeMockData - skips when disabled")
  void initializeMockData_disabled_skips() {
    // mockDataEnabled defaults to false (no @Value injection in unit test)
    initializer.initializeMockData();

    verify(elasticsearchOperations, never()).indexOps(any(Class.class));
    verify(slotRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("initializeMockData - creates caretakers and saves slots when enabled")
  void initializeMockData_enabled_createsData() throws Exception {
    enableMockData(1, 1);
    setupElasticIndex();
    setupRepositoriesForSingleCaretaker();

    when(slotGenerationService.generateSlotsForDay(any(), anyList(), anyList(), any())).thenReturn(
        List.of(new ElasticSlotDocument()));
    when(slotRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

    initializer.initializeMockData();

    verify(slotRepository).saveAll(anyList());
    verify(caretakerRepository).save(any(Caretaker.class));
  }

  @Test
  @DisplayName("initializeMockData - reuses existing user when found by email")
  void initializeMockData_existingUser_reuses() throws Exception {
    enableMockData(1, 0);
    setupElasticIndex();
    stubRRuleAndSlotGeneration();

    City city = new City();
    city.setCityId(1L);
    when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

    User existingUser = new User();
    existingUser.setUserId(UUID.randomUUID());
    when(userRepository.findByEmail("mock-caretaker-0@peti.com")).thenReturn(Optional.of(existingUser));

    Caretaker caretaker = new Caretaker();
    when(caretakerRepository.findByUserReference_UserId(existingUser.getUserId())).thenReturn(Optional.of(caretaker));

    initializer.initializeMockData();

    verify(userRepository, never()).save(any(User.class));
    verify(caretakerRepository, never()).save(any(Caretaker.class));
  }

  @Test
  @DisplayName("initializeMockData - recreates elastic index even if it exists")
  void initializeMockData_existingIndex_deletesAndRecreates() throws Exception {
    enableMockData(1, 0);
    when(elasticsearchOperations.indexOps(ElasticSlotDocument.class)).thenReturn(indexOperations);
    when(indexOperations.exists()).thenReturn(true);
    when(indexOperations.createMapping()).thenReturn(Document.create());
    stubRRuleAndSlotGeneration();

    City city = new City();
    city.setCityId(1L);
    when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
    User user = new User();
    user.setUserId(UUID.randomUUID());
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
    Caretaker caretaker = new Caretaker();
    when(caretakerRepository.findByUserReference_UserId(any())).thenReturn(Optional.of(caretaker));

    initializer.initializeMockData();

    verify(indexOperations).delete();
    verify(indexOperations).create();
    verify(indexOperations).putMapping(any(Document.class));
  }

  // ── Helpers ───────────────────────────────────────────────────────

  private void enableMockData(int caretakers, int daysAhead) throws Exception {
    setField("mockDataEnabled", true);
    setField("numberOfCaretakers", caretakers);
    setField("daysAhead", daysAhead);
  }

  private void setField(String name, Object value) throws Exception {
    var field = ElasticMockDataInitializer.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(initializer, value);
  }

  private void setupElasticIndex() {
    when(elasticsearchOperations.indexOps(ElasticSlotDocument.class)).thenReturn(indexOperations);
    when(indexOperations.exists()).thenReturn(false);
    when(indexOperations.createMapping()).thenReturn(Document.create());
  }

  private void stubRRuleAndSlotGeneration() {
    when(rruleRepository.save(any(CaretakerRRule.class))).thenAnswer(i -> {
      CaretakerRRule r = i.getArgument(0);
      r.setRruleId(UUID.randomUUID());
      return r;
    });
    when(slotGenerationService.generateSlotsForDay(any(), anyList(), anyList(), any())).thenReturn(
        List.of(new ElasticSlotDocument()));
    when(slotRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
  }

  private void setupRepositoriesForSingleCaretaker() {
    City city = new City();
    city.setCityId(1L);
    when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

    Role role = new Role();
    role.setRoleId(3);
    when(roleRepository.findById(3)).thenReturn(Optional.of(role));

    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    User savedUser = new User();
    savedUser.setUserId(UUID.randomUUID());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    when(caretakerRepository.findByUserReference_UserId(any())).thenReturn(Optional.empty());
    when(caretakerRepository.save(any(Caretaker.class))).thenAnswer(i -> i.getArgument(0));

    when(rruleRepository.save(any(CaretakerRRule.class))).thenAnswer(i -> {
      CaretakerRRule r = i.getArgument(0);
      r.setRruleId(UUID.randomUUID());
      return r;
    });
  }
}

