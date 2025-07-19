package com.peti.backend.service;

import com.peti.backend.dto.BreedDto;
import com.peti.backend.model.domain.Breed;
import com.peti.backend.repository.BreedRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BreedService {

  private final BreedRepository breedRepository;

  public List<BreedDto> getAllBreeds() {
    return breedRepository.findAll().stream().map(BreedDto::from).collect(Collectors.toList());
  }

  public BreedDto createBreed(BreedDto breedDto) {
    Breed savedBreed = breedRepository.save(toBreed(breedDto));
    return BreedDto.from(savedBreed);
  }

  public Optional<BreedDto> updateBreed(Integer id, BreedDto breedDto) {
    return breedRepository.findById(id)
        .map(breed -> {
          breed.setBreedName(breedDto.getBreedName());
          breed.setPetType(breedDto.getPetType());
          return breedRepository.save(breed);
        }).map(BreedDto::from);
  }

  public Optional<BreedDto> deleteBreed(Integer id) {
    return breedRepository.findById(id)
        .map(breed -> {
          breedRepository.deleteById(id);
          return BreedDto.from(breed);
        });
  }

  private Breed toBreed(BreedDto breedDto) {
    Breed breed = new Breed();
    breed.setBreedName(breedDto.getBreedName());
    breed.setPetType(breedDto.getPetType());
    return breed;
  }
}
