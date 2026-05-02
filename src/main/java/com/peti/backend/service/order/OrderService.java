package com.peti.backend.service.order;

import com.peti.backend.dto.order.OrderDto;
import com.peti.backend.dto.order.OrderModificationDto;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.domain.Order;
import com.peti.backend.model.domain.OrderModification;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.internal.EventStatus;
import com.peti.backend.model.internal.OrderStatus;
import com.peti.backend.repository.EventRepository;
import com.peti.backend.repository.OrderRepository;
import com.peti.backend.service.order.OrderStatusMachine.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final EventRepository eventRepository;

  /**
   * Caretaker approves the event and creates an order in {@link OrderStatus#RESERVED} status
   * in a single atomic action. The user is then expected to pay.
   */
  @Transactional
  public OrderDto reserveFromEvent(UUID eventId, UUID caretakerId) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

    if (!event.getCaretaker().getCaretakerId().equals(caretakerId)) {
      throw new BadRequestException("Event does not belong to this caretaker");
    }

    if (orderRepository.findByEvent_EventIdAndDeletedFalse(eventId).isPresent()) {
      throw new BadRequestException("Order already exists for event: " + eventId);
    }

    Order order = buildOrder(event);
    order.getModifications().add(
        OrderModification.create(order, OrderStatus.RESERVED, caretakerId,
            "Order reserved by caretaker"));
    event.setStatus(EventStatus.APPROVED);

    return OrderDto.from(orderRepository.save(order));
  }

  /**
   * Returns an order visible to the client.
   * Throws {@link NotFoundException} if the user is not the client on this order.
   */
  @Transactional(readOnly = true)
  public OrderDto getOrderAsClient(UUID orderId, UUID userId) {
    Order order = findActiveOrder(orderId);
    if (!order.getClient().getUserId().equals(userId)) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return OrderDto.from(order);
  }

  /**
   * Returns an order visible to the caretaker.
   * Throws {@link NotFoundException} if the caretaker is not on this order.
   */
  @Transactional(readOnly = true)
  public OrderDto getOrderAsCaretaker(UUID orderId, UUID caretakerId) {
    Order order = findActiveOrder(orderId);
    if (!order.getCaretaker().getCaretakerId().equals(caretakerId)) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return OrderDto.from(order);
  }

  /**
   * Returns modification history for the client view.
   */
  @Transactional(readOnly = true)
  public List<OrderModificationDto> getModificationsAsClient(UUID orderId, UUID userId) {
    Order order = findActiveOrder(orderId);
    if (!order.getClient().getUserId().equals(userId)) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return toModificationDtos(order);
  }

  /**
   * Returns modification history for the caretaker view.
   */
  @Transactional(readOnly = true)
  public List<OrderModificationDto> getModificationsAsCaretaker(UUID orderId, UUID caretakerId) {
    Order order = findActiveOrder(orderId);
    if (!order.getCaretaker().getCaretakerId().equals(caretakerId)) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return toModificationDtos(order);
  }

  @Transactional(readOnly = true)
  public List<OrderDto> getOrdersByClientId(UUID clientId) {
    return orderRepository.findAllByClient_UserIdAndDeletedFalse(clientId).stream()
        .map(OrderDto::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<OrderDto> getOrdersByCaretakerId(UUID caretakerId) {
    return orderRepository.findAllByCaretaker_CaretakerIdAndDeletedFalse(caretakerId).stream()
        .map(OrderDto::from)
        .toList();
  }

  /**
   * Transitions an order to the target status. The caller must pass the actor's {@link Role}
   * explicitly — it is determined by the URL path (client vs caretaker endpoint), not resolved
   * at runtime.
   */
  @Transactional
  public OrderDto transition(UUID orderId, OrderStatus targetStatus,
      UUID actorId, Role actorRole, String comment) {
    Order order = findActiveOrder(orderId);

    if (!OrderStatusMachine.isAuthorized(targetStatus, actorRole)) {
      throw new BadRequestException("Not authorized to transition order to " + targetStatus);
    }

    if (!OrderStatusMachine.canTransition(order.getStatus(), targetStatus)) {
      throw new BadRequestException(
          "Cannot transition from " + order.getStatus() + " to " + targetStatus);
    }

    order.setStatus(targetStatus);
    order.setUpdatedAt(LocalDateTime.now());
    order.getModifications().add(
        OrderModification.create(order, targetStatus, actorId, comment));

    return OrderDto.from(orderRepository.save(order));
  }


  private Order findActiveOrder(UUID orderId) {
    return orderRepository.findByOrderIdAndDeletedFalse(orderId)
        .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
  }

  private List<OrderModificationDto> toModificationDtos(Order order) {
    return order.getModifications().stream()
        .map(OrderModificationDto::from)
        .toList();
  }

  private Order buildOrder(Event event) {
    BigDecimal totalPrice = event.getPrice() != null
        ? event.getPrice().totalPrice() : BigDecimal.ZERO;
    String currency = event.getPrice() != null ? event.getPrice().currency() : "UAH";

    Order order = new Order();
    order.setClient(event.getUser());
    order.setCaretaker(event.getCaretaker());
    order.setEvent(event);
    order.setPrice(totalPrice);
    order.setCurrency(currency);
    order.setStatus(OrderStatus.RESERVED);
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    order.setDeleted(false);
    return order;
  }
}

