package com.peti.backend.repository;

import com.peti.backend.model.Pet;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {

  List<Pet> findAllByPetOwner_UserId(UUID ownerId);
}
