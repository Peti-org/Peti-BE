package com.peti.backend.service.user;

import com.peti.backend.dto.pet.PetDto;
import com.peti.backend.dto.pet.RequestPetDto;
import com.peti.backend.model.domain.Breed;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.repository.PetRepository;
import com.peti.backend.utils.DeepCloner;
import jakarta.persistence.EntityManager;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetService {

  private final PetRepository petRepository;
  private final EntityManager entityManager;
  private final DeepCloner deepCloner;

  public List<PetDto> getAllPets(UserProjection user) {
    List<Pet> pets = petRepository.findAllByPetOwner_UserId(user.getUserId());
    return pets.stream()
        .map(this::mapToDto)
        .toList();
  }

  public Optional<PetDto> getPetById(UUID id, UserProjection user) {
    return petRepository.findById(id)
        .filter(p -> p.getPetOwner().getUserId().equals(user.getUserId()))
        .map(this::mapToDto);
  }

  @Transactional
  public PetDto createPet(RequestPetDto requestPet, UserProjection user) {
    Pet savedPet = petRepository.save(toPet(requestPet, user.getUserId()));
    return mapToDto(savedPet);
  }

  @Transactional
  public Optional<PetDto> updatePet(UUID id, RequestPetDto requestPetDto, UserProjection user) {
    return petRepository.findById(id)
        .filter(p -> p.getPetOwner().getUserId().equals(user.getUserId()))
        .map(pet -> {
          pet.setName(requestPetDto.getName());
          pet.setBirthday(Date.valueOf(requestPetDto.getDateOfBirth()));
          pet.setContext(requestPetDto.getProfile());
          pet.setBreed(new Breed(requestPetDto.getBreedId()));
          return petRepository.save(pet);
        })
        .map(this::mapToDto);
  }

  public Optional<PetDto> deletePet(UUID id, UserProjection user) {
    return petRepository.findById(id)
        .filter(p -> p.getPetOwner().getUserId().equals(user.getUserId()))
        .map(pet -> {
          petRepository.deleteById(id);
          return mapToDto(pet);
        });
  }

  private PetDto mapToDto(Pet pet) {
    return PetDto.convert(pet, deepCloner.deepCopyPetProfile(pet.getContext()));
  }

  private Pet toPet(RequestPetDto petDto, UUID petOwnerId) {
    Pet pet = new Pet();
    pet.setName(petDto.getName());
    pet.setBirthday(Date.valueOf(petDto.getDateOfBirth()));
    pet.setBreed(entityManager.getReference(Breed.class, petDto.getBreedId()));
    pet.setPetOwner(entityManager.getReference(User.class, petOwnerId));
    pet.setContext(deepCloner.deepCopyPetProfile(petDto.getProfile()));
    pet.setPetDataFolder("default"); //todo write anouther based on id
    return pet;
  }
}
