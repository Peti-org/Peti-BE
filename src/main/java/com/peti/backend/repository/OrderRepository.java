package com.peti.backend.repository;

import com.peti.backend.model.domain.Order;
import com.peti.backend.model.internal.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>,
    JpaSpecificationExecutor<Order> {

  List<Order> findAllByClient_UserIdAndDeletedFalse(UUID clientId);

  List<Order> findAllByCaretaker_CaretakerIdAndDeletedFalse(UUID caretakerId);

  Optional<Order> findByOrderIdAndDeletedFalse(UUID orderId);

  Optional<Order> findByEvent_EventIdAndDeletedFalse(UUID eventId);

  List<Order> findAllByStatusAndEvent_DatetimeFromLessThanEqual(
      OrderStatus status, LocalDateTime dateTime);

  List<Order> findAllByStatusAndEvent_DatetimeToLessThanEqual(
      OrderStatus status, LocalDateTime dateTime);

  /** Admin: find by ID including deleted orders. */
  Optional<Order> findByOrderId(UUID orderId);

  /** Admin: list all orders (including deleted) with pagination. */
  Page<Order> findAll(Pageable pageable);
}

