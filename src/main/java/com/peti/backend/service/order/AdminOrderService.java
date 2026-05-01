package com.peti.backend.service.order;

import com.peti.backend.dto.order.AdminOrderDto;
import com.peti.backend.dto.order.OrderModificationDto;
import com.peti.backend.dto.order.UserInfoDto;
import com.peti.backend.model.domain.Order;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.internal.OrderStatus;
import com.peti.backend.repository.OrderRepository;
import com.peti.backend.repository.OrderSpecifications;
import com.peti.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-facing read operations on orders. Sees deleted orders, full user info,
 * and supports filtering + pagination.
 */
@Service
@RequiredArgsConstructor
public class AdminOrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<AdminOrderDto> findOrders(OrderStatus status,
      UUID caretakerId, UUID clientId,
      LocalDateTime from, LocalDateTime to,
      Pageable pageable) {
    Specification<Order> spec = Specification.allOf(
        OrderSpecifications.byStatus(status),
        OrderSpecifications.byCaretakerId(caretakerId),
        OrderSpecifications.byClientId(clientId),
        OrderSpecifications.createdBetween(from, to)
    );
    return orderRepository.findAll(spec, pageable).map(AdminOrderDto::from);
  }

  @Transactional(readOnly = true)
  public AdminOrderDto getOrder(UUID orderId) {
    Order order = orderRepository.findByOrderId(orderId)
        .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    return AdminOrderDto.from(order);
  }

  @Transactional(readOnly = true)
  public List<OrderModificationDto> getModifications(UUID orderId) {
    Order order = orderRepository.findByOrderId(orderId)
        .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

    Set<UUID> actorIds = order.getModifications().stream()
        .map(m -> m.getActorId())
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toSet());

    Map<UUID, UserInfoDto> userInfoMap = new HashMap<>();
    for (User u : userRepository.findAllById(actorIds)) {
      userInfoMap.put(u.getUserId(), UserInfoDto.from(u));
    }

    return order.getModifications().stream()
        .map(m -> OrderModificationDto.fromAdmin(m, userInfoMap.get(m.getActorId())))
        .toList();
  }
}

