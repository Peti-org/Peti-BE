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

    Order saved = orderRepository.save(order);
    return OrderDto.from(saved);
  }

  /**
   * Returns an order if the requesting actor is a participant (client or caretaker).
   * Throws {@link NotFoundException} otherwise to avoid leaking existence.
   */
  @Transactional(readOnly = true)
  public OrderDto getOrderForActor(UUID orderId, UUID actorId) {
    Order order = findActiveOrder(orderId);
    if (!OrderStatusMachine.canView(actorId,
        order.getClient().getUserId(),
        order.getCaretaker().getCaretakerId())) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return OrderDto.from(order);
  }

  /**
   * Returns the modification history of the order, but only if the requesting actor
   * is the client or caretaker on the order.
   */
  @Transactional(readOnly = true)
  public List<OrderModificationDto> getModificationsForActor(UUID orderId, UUID actorId) {
    Order order = findActiveOrder(orderId);
    if (!OrderStatusMachine.canView(actorId,
        order.getClient().getUserId(),
        order.getCaretaker().getCaretakerId())) {
      throw new NotFoundException("Order not found: " + orderId);
    }
    return order.getModifications().stream()
        .map(OrderModificationDto::from)
        .toList();
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

  @Transactional
  public OrderDto transition(UUID orderId, OrderStatus targetStatus,
      UUID actorId, String comment) {
    Order order = findActiveOrder(orderId);

    OrderStatusMachine.Role role = OrderStatusMachine.resolveRole(actorId,
        order.getClient().getUserId(),
        order.getCaretaker().getCaretakerId());

    if (!OrderStatusMachine.isAuthorized(targetStatus, role)) {
      throw new BadRequestException(
          "Not authorized to transition order to " + targetStatus);
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

