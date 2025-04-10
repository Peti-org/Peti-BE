package com.peti.backend.service;

import com.peti.backend.dto.PetDto;
import com.peti.backend.model.Pet;
import com.peti.backend.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    @Autowired private PetRepository petRepository;

    public List<PetDto> getAllPets() {
        List<Pet> pets = petRepository.findAll();
        List<PetDto> petDtos = new ArrayList<>();
        for (Pet pet : pets) {
            petDtos.add(mapToDto(pet));
        }
        return petDtos;
    }

    public Optional<PetDto> getPetById(Long id) {
        return petRepository.findById(id).map(this::mapToDto);
    }

    public PetDto createPet(Pet pet) {
        Pet savedPet = petRepository.save(pet);
        return mapToDto(savedPet);
    }

    private PetDto mapToDto(Pet pet) {
        PetDto dto = new PetDto();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setAge(pet.getAge());
        if(pet.getBreed()!=null){
            dto.setBreedId(pet.getBreed().getId());
        }
        if(pet.getCaretaker()!=null){
            dto.setCaretakerId(pet.getCaretaker().getId());
        }


        return dto;
    }

    public Optional<Pet> updatePet(Long id, Pet petDetails) {
        return petRepository.findById(id).map(pet -> {
            pet.setName(petDetails.getName());
            pet.setBreed(petDetails.getBreed());
            pet.setAge(petDetails.getAge());
            pet.setCaretaker(petDetails.getCaretaker());
            return petRepository.save(pet);
        });
    
    
    }

    public Optional<PetDto> deletePet(Long id) {
        if (petRepository.existsById(id)) {
            Optional<Pet> petOptional=petRepository.findById(id);
            if(!petOptional.isPresent())return Optional.empty();
            petRepository.deleteById(id);
            return Optional.of(mapToDto(petOptional.get()));
        }
        return Optional.empty();
    }
}