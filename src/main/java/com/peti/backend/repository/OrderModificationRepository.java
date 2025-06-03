package com.peti.backend.repository;

import com.peti.backend.model.OrderModification;
import com.peti.backend.model.OrderModificationPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderModificationRepository extends JpaRepository<OrderModification, OrderModificationPK> {
}