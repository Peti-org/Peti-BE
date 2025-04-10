package com.peti.backend.service;

import com.peti.backend.dto.BreedDto;
import com.peti.backend.model.Breed;
import com.peti.backend.repository.BreedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BreedService {

    @Autowired
    private BreedRepository breedRepository;

    public List<BreedDto> getAllBreeds() {
        List<Breed> breeds = breedRepository.findAll();
        return breeds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<BreedDto> getBreedById(Long id) {
        return breedRepository.findById(id).map(this::convertToDto);
    }

    public BreedDto createBreed(Breed breed) {
        Breed savedBreed = breedRepository.save(breed);
        return convertToDto(savedBreed);
    }

    public BreedDto updateBreed(Long id, Breed breed) {
        Optional<Breed> breedData = breedRepository.findById(id);

        if (breedData.isPresent()) {
            Breed _breed = breedData.get();
            _breed.setName(breed.getName());
            return convertToDto(breedRepository.save(_breed));
        } else {
            return null;
        }
    }

    public void deleteBreed(Long id) {
        breedRepository.deleteById(id);
    }

    private BreedDto convertToDto(Breed breed) {
        BreedDto breedDto = new BreedDto();
        breedDto.setId(breed.getId());
        breedDto.setName(breed.getName());
        return breedDto;
    }
    }
}