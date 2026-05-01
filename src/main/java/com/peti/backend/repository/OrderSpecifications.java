package com.peti.backend.repository;

import com.peti.backend.model.domain.Order;
import com.peti.backend.model.internal.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for filtering orders in admin queries.
 */
public final class OrderSpecifications {

  private OrderSpecifications() {
  }

  public static Specification<Order> byStatus(OrderStatus status) {
    return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
  }

  public static Specification<Order> byCaretakerId(UUID caretakerId) {
    return (root, query, cb) -> caretakerId == null
        ? null
        : cb.equal(root.get("caretaker").get("caretakerId"), caretakerId);
  }

  public static Specification<Order> byClientId(UUID clientId) {
    return (root, query, cb) -> clientId == null
        ? null
        : cb.equal(root.get("client").get("userId"), clientId);
  }

  public static Specification<Order> createdBetween(LocalDateTime from, LocalDateTime to) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (from != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
      }
      if (to != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
      }
      return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}

