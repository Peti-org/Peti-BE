package com.peti.backend.repository;

import com.peti.backend.model.domain.OrderModification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderModificationRepository extends JpaRepository<OrderModification, UUID> {
}

