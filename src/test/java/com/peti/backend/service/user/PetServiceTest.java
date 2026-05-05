package com.peti.backend.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import com.peti.backend.dto.pet.PetDto;
import com.peti.backend.dto.pet.PetProfile;
import com.peti.backend.dto.pet.PetProfile.Sex;
import com.peti.backend.dto.pet.PetProfile.TriState;
import com.peti.backend.dto.pet.RequestPetDto;
import com.peti.backend.model.domain.Breed;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.repository.PetRepository;
import com.peti.backend.utils.DeepCloner;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

  @Mock
  private PetRepository petRepository;
  @Mock
  private EntityManager entityManager;
  @Mock
  private DeepCloner deepCloner;

  @InjectMocks
  private PetService petService;

  private UUID userId;
  private UserProjection userProjection;
  private Pet pet;
  private PetProfile profile;
  private Breed breed;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    userProjection = new UserProjection(userId, "alice@test.com", "pass", 1);

    profile = new PetProfile(
        new BigDecimal("12.5"),
        Sex.MALE,
        TriState.YES,
        TriState.YES,
        TriState.YES,
        TriState.UNKNOWN,
        TriState.YES,
        null,
        null,
        "VetClinic Kyiv",
        "Loves walks",
        "Friendly golden retriever"
    );

    breed = new Breed(1);
    breed.setPetType("Dog");
    breed.setBreedName("Golden Retriever");

    User owner = new User(userId);

    pet = new Pet();
    pet.setPetId(UUID.randomUUID());
    pet.setName("Buddy");
    pet.setBirthday(Date.valueOf("2020-01-15"));
    pet.setContext(profile);
    pet.setPetDataFolder("default");
    pet.setPetOwner(owner);
    pet.setBreed(breed);

    lenient().when(deepCloner.deepCopyPetProfile(any())).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void getAllPets_returnsPetsWithProfile() {
    when(petRepository.findAllByPetOwner_UserId(userId)).thenReturn(List.of(pet));

    List<PetDto> result = petService.getAllPets(userProjection);

    assertEquals(1, result.size());
    assertNotNull(result.get(0).getProfile());
    assertEquals(Sex.MALE, result.get(0).getProfile().sex());
    assertEquals(TriState.YES, result.get(0).getProfile().sterilized());
    assertEquals(new BigDecimal("12.5"), result.get(0).getProfile().weightKg());
  }

  @Test
  void getAllPets_empty_returnsEmptyList() {
    when(petRepository.findAllByPetOwner_UserId(userId)).thenReturn(Collections.emptyList());

    List<PetDto> result = petService.getAllPets(userProjection);

    assertTrue(result.isEmpty());
  }

  @Test
  void getPetById_ownPet_returnsWithProfile() {
    when(petRepository.findById(pet.getPetId())).thenReturn(Optional.of(pet));

    Optional<PetDto> result = petService.getPetById(pet.getPetId(), userProjection);

    assertTrue(result.isPresent());
    assertEquals("Buddy", result.get().getName());
    assertEquals(TriState.YES, result.get().getProfile().vaccinated());
    assertEquals("VetClinic Kyiv", result.get().getProfile().vetInfo());
  }

  @Test
  void getPetById_otherUserPet_returnsEmpty() {
    User otherOwner = new User(UUID.randomUUID());
    pet.setPetOwner(otherOwner);
    when(petRepository.findById(pet.getPetId())).thenReturn(Optional.of(pet));

    Optional<PetDto> result = petService.getPetById(pet.getPetId(), userProjection);

    assertTrue(result.isEmpty());
  }

  @Test
  void getPetById_notFound_returnsEmpty() {
    UUID id = UUID.randomUUID();
    when(petRepository.findById(id)).thenReturn(Optional.empty());

    Optional<PetDto> result = petService.getPetById(id, userProjection);

    assertTrue(result.isEmpty());
  }

  @Test
  void createPet_savesWithProfile() {
    when(entityManager.getReference(Breed.class, 1)).thenReturn(breed);
    when(entityManager.getReference(User.class, userId)).thenReturn(new User(userId));
    when(petRepository.save(any(Pet.class))).thenAnswer(inv -> {
      Pet saved = inv.getArgument(0);
      saved.setPetId(UUID.randomUUID());
      saved.setBreed(breed);
      return saved;
    });

    RequestPetDto request = createRequestPetDto();
    PetDto result = petService.createPet(request, userProjection);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals(Sex.MALE, result.getProfile().sex());
    verify(petRepository).save(any(Pet.class));
  }

  @Test
  void updatePet_updatesProfile() {
    when(petRepository.findById(pet.getPetId())).thenReturn(Optional.of(pet));
    when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

    PetProfile updatedProfile = new PetProfile(
        new BigDecimal("14.0"), Sex.MALE, TriState.YES, TriState.YES,
        TriState.NO, TriState.YES, TriState.YES,
        "Grain allergy", "Apoquel daily", "New Vet Clinic", "Updated details", "Updated description"
    );

    RequestPetDto request = createRequestPetDto(updatedProfile);
    Optional<PetDto> result = petService.updatePet(pet.getPetId(), request, userProjection);

    assertTrue(result.isPresent());
    assertEquals(new BigDecimal("14.0"), result.get().getProfile().weightKg());
    assertEquals("Grain allergy", result.get().getProfile().allergies());
    assertEquals(TriState.NO, result.get().getProfile().getsAlongWithDogs());
  }

  @Test
  void deletePet_ownPet_deletesAndReturns() {
    when(petRepository.findById(pet.getPetId())).thenReturn(Optional.of(pet));

    Optional<PetDto> result = petService.deletePet(pet.getPetId(), userProjection);

    assertTrue(result.isPresent());
    assertEquals("Buddy", result.get().getName());
    verify(petRepository).deleteById(pet.getPetId());
  }

  @Test
  void deletePet_otherUserPet_returnsEmpty() {
    User otherOwner = new User(UUID.randomUUID());
    pet.setPetOwner(otherOwner);
    when(petRepository.findById(pet.getPetId())).thenReturn(Optional.of(pet));

    Optional<PetDto> result = petService.deletePet(pet.getPetId(), userProjection);

    assertTrue(result.isEmpty());
  }

  private RequestPetDto createRequestPetDto() {
    return createRequestPetDto(profile);
  }

  private RequestPetDto createRequestPetDto(PetProfile petProfile) {
    // Use reflection since RequestPetDto uses private fields with no-arg constructor
    try {
      RequestPetDto dto = new RequestPetDto();
      setField(dto, "name", "Buddy");
      setField(dto, "dateOfBirth", java.time.LocalDate.of(2020, 1, 15));
      setField(dto, "breedId", 1);
      setField(dto, "profile", petProfile);
      return dto;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setField(Object obj, String fieldName, Object value) throws Exception {
    var field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }
}

