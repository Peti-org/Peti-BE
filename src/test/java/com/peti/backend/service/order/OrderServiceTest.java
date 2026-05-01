package com.peti.backend.service.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.order.OrderDto;
import com.peti.backend.dto.order.OrderModificationDto;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.domain.Order;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.internal.OrderStatus;
import com.peti.backend.repository.EventRepository;
import com.peti.backend.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CARETAKER_ID = UUID.fromString("b1a7e8e2-8c2e-4c1a-9e2a-123456789abc");
  private static final UUID EVENT_ID = UUID.fromString("eeee1111-1111-1111-1111-111111111111");
  private static final UUID ORDER_ID = UUID.fromString("aaaa1111-1111-1111-1111-111111111111");

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private EventRepository eventRepository;

  @InjectMocks
  private OrderService orderService;

  @Test
  @DisplayName("reserveFromEvent - success creates RESERVED order matching expected DTO")
  void reserveFromEvent_success() {
    Event event = ResourceLoader.loadResource("event-entity.json", Event.class);
    OrderDto expected =
        ResourceLoader.loadResource("order-reserved-response.json", OrderDto.class);

    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
    when(orderRepository.findByEvent_EventIdAndDeletedFalse(EVENT_ID))
        .thenReturn(Optional.empty());
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
      Order o = inv.getArgument(0);
      o.setOrderId(ORDER_ID);
      return o;
    });

    OrderDto result = orderService.reserveFromEvent(EVENT_ID, CARETAKER_ID);

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringFields("updatedAt")
        .isEqualTo(expected);
  }

  @Test
  @DisplayName("reserveFromEvent - event not found throws NotFoundException")
  void reserveFromEvent_eventNotFound() {
    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.reserveFromEvent(EVENT_ID, CARETAKER_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Event not found");
  }

  @Test
  @DisplayName("reserveFromEvent - wrong caretaker throws BadRequestException")
  void reserveFromEvent_wrongCaretaker() {
    Event event = ResourceLoader.loadResource("event-entity.json", Event.class);
    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

    UUID wrongCaretaker = UUID.randomUUID();
    assertThatThrownBy(() -> orderService.reserveFromEvent(EVENT_ID, wrongCaretaker))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("does not belong");
  }

  @Test
  @DisplayName("reserveFromEvent - duplicate order throws BadRequestException")
  void reserveFromEvent_duplicateOrder() {
    Event event = ResourceLoader.loadResource("event-entity.json", Event.class);
    Order existing = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
    when(orderRepository.findByEvent_EventIdAndDeletedFalse(EVENT_ID))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> orderService.reserveFromEvent(EVENT_ID, CARETAKER_ID))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  @DisplayName("getOrderForActor - participant client gets the order")
  void getOrderForActor_clientCanView() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    OrderDto expected = ResourceLoader.loadResource("order-response.json", OrderDto.class);
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.of(order));

    OrderDto result = orderService.getOrderForActor(ORDER_ID, CLIENT_ID);

    assertThat(result).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  @DisplayName("getOrderForActor - non-participant gets NotFoundException (no leak)")
  void getOrderForActor_strangerGets404() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.of(order));

    UUID stranger = UUID.randomUUID();
    assertThatThrownBy(() -> orderService.getOrderForActor(ORDER_ID, stranger))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("getOrderForActor - missing order throws NotFoundException")
  void getOrderForActor_notFound() {
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.getOrderForActor(ORDER_ID, CLIENT_ID))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("getModificationsForActor - non-participant rejected")
  void getModificationsForActor_strangerRejected() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.of(order));

    UUID stranger = UUID.randomUUID();
    assertThatThrownBy(() -> orderService.getModificationsForActor(ORDER_ID, stranger))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("getModificationsForActor - participant gets list")
  void getModificationsForActor_participantGetsList() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.of(order));

    List<OrderModificationDto> mods =
        orderService.getModificationsForActor(ORDER_ID, CLIENT_ID);

    assertThat(mods).isEmpty(); // fixture has no modifications
  }

  @Test
  @DisplayName("getOrdersByClientId - returns deserialized expected list")
  void getOrdersByClientId_returnsList() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    List<OrderDto> expected = List.of(
        ResourceLoader.loadResource("order-response.json", OrderDto.class));

    when(orderRepository.findAllByClient_UserIdAndDeletedFalse(CLIENT_ID))
        .thenReturn(List.of(order));

    List<OrderDto> result = orderService.getOrdersByClientId(CLIENT_ID);

    assertThat(result).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  @DisplayName("transition - valid RESERVED to DEFERRED_PAYMENT by client succeeds")
  void transition_reservedToDeferredPaymentByClient() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    OrderDto result = orderService.transition(
        ORDER_ID, OrderStatus.DEFERRED_PAYMENT, CLIENT_ID, "Paid");

    assertThat(result.status()).isEqualTo(OrderStatus.DEFERRED_PAYMENT);
  }

  @Test
  @DisplayName("transition - invalid RESERVED to PAID throws BadRequestException")
  void transition_invalidTransition() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.transition(
        ORDER_ID, OrderStatus.PAID, CARETAKER_ID, null))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Cannot transition");
  }

  @Test
  @DisplayName("transition - client cannot decline (only caretaker can)")
  void transition_clientCannotDecline() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.transition(
        ORDER_ID, OrderStatus.DECLINED, CLIENT_ID, null))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Not authorized");
  }

  @Test
  @DisplayName("transition - unauthorized actor throws BadRequestException")
  void transition_unauthorizedActor() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(orderRepository.findByOrderIdAndDeletedFalse(ORDER_ID))
        .thenReturn(Optional.of(order));

    UUID stranger = UUID.randomUUID();
    assertThatThrownBy(() -> orderService.transition(
        ORDER_ID, OrderStatus.CANCELLED, stranger, null))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Not authorized");
  }

  /** Ensures TypeReference import is used (kept for future list-typed fixtures). */
  @SuppressWarnings("unused")
  private static final TypeReference<List<OrderDto>> ORDER_LIST_TYPE = new TypeReference<>() {};
}

