package com.peti.backend.repository;

import com.peti.backend.model.CaretakerSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CaretakerSlotRepository extends JpaRepository<CaretakerSlot, UUID> {
}