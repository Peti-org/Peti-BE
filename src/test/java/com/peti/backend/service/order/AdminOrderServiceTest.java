package com.peti.backend.service.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.order.AdminOrderDto;
import com.peti.backend.dto.order.OrderModificationDto;
import com.peti.backend.model.domain.Order;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.internal.OrderStatus;
import com.peti.backend.repository.OrderRepository;
import com.peti.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

  private static final UUID ORDER_ID = UUID.fromString("aaaa1111-1111-1111-1111-111111111111");
  private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CARETAKER_USER_ID = UUID.fromString("b1a7e8e2-8c2e-4c1a-9e2a-123456789abc");
  private static final UUID CARETAKER_ID = CARETAKER_USER_ID;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private AdminOrderService adminOrderService;

  // ---------- findOrders ----------

  @Test
  @DisplayName("findOrders - returns mapped page using all filters")
  void findOrders_returnsMappedPage() {
    Order order = ResourceLoader.loadResource(
        "order-with-modifications-entity.json", Order.class);
    AdminOrderDto expected = ResourceLoader.loadResource(
        "admin-order-response.json", AdminOrderDto.class);
    Pageable pageable = PageRequest.of(0, 20);
    Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

    when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<AdminOrderDto> result = adminOrderService.findOrders(
        OrderStatus.DEFERRED_PAYMENT, CARETAKER_ID, CLIENT_ID,
        LocalDateTime.of(2026, 4, 1, 0, 0),
        LocalDateTime.of(2026, 4, 30, 23, 59),
        pageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent())
        .singleElement()
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  @Test
  @DisplayName("findOrders - empty result returns empty page")
  void findOrders_emptyResult() {
    Pageable pageable = PageRequest.of(0, 20);
    when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty(pageable));

    Page<AdminOrderDto> result = adminOrderService.findOrders(
        null, null, null, null, null, pageable);

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  @Test
  @DisplayName("findOrders - all-null filters still calls repository with composed spec")
  void findOrders_allNullFilters() {
    Pageable pageable = PageRequest.of(0, 10);
    when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty(pageable));

    adminOrderService.findOrders(null, null, null, null, null, pageable);

    ArgumentCaptor<Specification<Order>> specCaptor =
        ArgumentCaptor.forClass(Specification.class);
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(orderRepository).findAll(specCaptor.capture(), pageableCaptor.capture());
    assertThat(specCaptor.getValue()).isNotNull();
    assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
  }

  // ---------- getOrder ----------

  @Test
  @DisplayName("getOrder - found returns AdminOrderDto matching expected fixture")
  void getOrder_found() {
    Order order = ResourceLoader.loadResource(
        "order-with-modifications-entity.json", Order.class);
    AdminOrderDto expected = ResourceLoader.loadResource(
        "admin-order-response.json", AdminOrderDto.class);

    when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

    AdminOrderDto result = adminOrderService.getOrder(ORDER_ID);

    assertThat(result).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  @DisplayName("getOrder - includes deleted orders (admin sees them)")
  void getOrder_includesDeleted() {
    Order order = ResourceLoader.loadResource(
        "order-with-modifications-entity.json", Order.class);
    order.setDeleted(true);
    when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

    AdminOrderDto result = adminOrderService.getOrder(ORDER_ID);

    assertThat(result.deleted()).isTrue();
  }

  @Test
  @DisplayName("getOrder - missing order throws NotFoundException")
  void getOrder_notFound() {
    when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminOrderService.getOrder(ORDER_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Order not found");
  }

  // ---------- getModifications ----------

  @Test
  @DisplayName("getModifications - returns list with full actor info from fixtures")
  void getModifications_withActorInfo() {
    Order order = loadOrderWithModifications();
    User caretakerUser = ResourceLoader.loadResource(
        "caretaker-user-entity.json", User.class);
    User clientUser = ResourceLoader.loadResource("user-entity.json", User.class);
    List<OrderModificationDto> expected = ResourceLoader.loadResource(
        "admin-modifications-response.json", new TypeReference<>() {
        });

    when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
    when(userRepository.findAllById(anyCollection()))
        .thenReturn(List.of(caretakerUser, clientUser));

    List<OrderModificationDto> result = adminOrderService.getModifications(ORDER_ID);

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expected);
  }

  @Test
  @DisplayName("getModifications - missing actor user yields null actor field, no exception")
  void getModifications_missingActorUser() {
    Order order = loadOrderWithModifications();
    when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
    when(userRepository.findAllById(anyCollection())).thenReturn(List.of());

    List<OrderModificationDto> result = adminOrderService.getModifications(ORDER_ID);

    assertThat(result).hasSize(2);
    assertThat(result).allSatisfy(m -> assertThat(m.actor()).isNull());
    assertThat(result).allSatisfy(m -> assertThat(m.actorId()).isNotNull());
  }

  @Test
  @DisplayName("getModifications - empty modification history returns empty list, no user lookup")
  void getModifications_emptyHistory() {
    Order order = ResourceLoader.loadResource("order-entity.json", Order.class);
    when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
    when(userRepository.findAllById(anyCollection())).thenReturn(List.of());

    List<OrderModificationDto> result = adminOrderService.getModifications(ORDER_ID);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("getModifications - missing order throws NotFoundException, no user lookup")
  void getModifications_orderNotFound() {
    when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminOrderService.getModifications(ORDER_ID))
        .isInstanceOf(NotFoundException.class);

    verify(userRepository, never()).findAllById(anyCollection());
  }

  /**
   * Loads the fixture and wires the {@code modification.order} back-reference,
   * which JSON deserialization cannot reconstruct (avoids infinite recursion).
   */
  private Order loadOrderWithModifications() {
    Order order = ResourceLoader.loadResource(
        "order-with-modifications-entity.json", Order.class);
    order.getModifications().forEach(m -> m.setOrder(order));
    return order;
  }
}

