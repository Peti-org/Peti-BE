package com.peti.backend.service.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.model.internal.OrderStatus;
import com.peti.backend.service.order.OrderStatusMachine.Role;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderStatusMachineTest {

  private static final UUID CLIENT = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CARETAKER = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID STRANGER = UUID.fromString("33333333-3333-3333-3333-333333333333");

  @Test
  @DisplayName("RESERVED can transition to DEFERRED_PAYMENT, DECLINED, CANCELLED")
  void reserved_allowedTransitions() {
    assertThat(OrderStatusMachine.canTransition(OrderStatus.RESERVED,
        OrderStatus.DEFERRED_PAYMENT)).isTrue();
    assertThat(OrderStatusMachine.canTransition(OrderStatus.RESERVED, OrderStatus.DECLINED))
        .isTrue();
    assertThat(OrderStatusMachine.canTransition(OrderStatus.RESERVED, OrderStatus.CANCELLED))
        .isTrue();
  }

  @Test
  @DisplayName("DEFERRED_PAYMENT can transition to EXECUTING or CANCELLED")
  void deferredPayment_allowedTransitions() {
    assertThat(OrderStatusMachine.canTransition(OrderStatus.DEFERRED_PAYMENT,
        OrderStatus.EXECUTING)).isTrue();
    assertThat(OrderStatusMachine.canTransition(OrderStatus.DEFERRED_PAYMENT,
        OrderStatus.CANCELLED)).isTrue();
  }

  @Test
  @DisplayName("DEFERRED_PAYMENT cannot go back to RESERVED")
  void deferredPayment_cannotGoBack() {
    assertThat(OrderStatusMachine.canTransition(OrderStatus.DEFERRED_PAYMENT,
        OrderStatus.RESERVED)).isFalse();
  }

  @Test
  @DisplayName("EXECUTING can only transition to FINISHED")
  void executing_onlyToFinished() {
    assertThat(OrderStatusMachine.canTransition(OrderStatus.EXECUTING, OrderStatus.FINISHED))
        .isTrue();
    assertThat(OrderStatusMachine.canTransition(OrderStatus.EXECUTING, OrderStatus.CANCELLED))
        .isFalse();
  }

  @Test
  @DisplayName("FINISHED can only transition to PAID")
  void finished_onlyToPaid() {
    assertThat(OrderStatusMachine.canTransition(OrderStatus.FINISHED, OrderStatus.PAID))
        .isTrue();
    assertThat(OrderStatusMachine.canTransition(OrderStatus.FINISHED, OrderStatus.CANCELLED))
        .isFalse();
  }

  @Test
  @DisplayName("Terminal statuses have no transitions")
  void terminalStatuses_noTransitions() {
    assertThat(OrderStatusMachine.isTerminal(OrderStatus.PAID)).isTrue();
    assertThat(OrderStatusMachine.isTerminal(OrderStatus.DECLINED)).isTrue();
    assertThat(OrderStatusMachine.isTerminal(OrderStatus.CANCELLED)).isTrue();
  }

  @Test
  @DisplayName("isAuthorized: only caretaker can DECLINE")
  void declinePermission() {
    assertThat(OrderStatusMachine.isAuthorized(OrderStatus.DECLINED, Role.CARETAKER)).isTrue();
    assertThat(OrderStatusMachine.isAuthorized(OrderStatus.DECLINED, Role.CLIENT)).isFalse();
    assertThat(OrderStatusMachine.isAuthorized(OrderStatus.DECLINED, null)).isFalse();
  }

  @Test
  @DisplayName("isAuthorized: only client can pay (DEFERRED_PAYMENT)")
  void payPermission() {
    assertThat(OrderStatusMachine.isAuthorized(OrderStatus.DEFERRED_PAYMENT, Role.CLIENT))
        .isTrue();
    assertThat(OrderStatusMachine.isAuthorized(OrderStatus.DEFERRED_PAYMENT, Role.CARETAKER))
        .isFalse();
  }

  @Test
  @DisplayName("isAuthorized: both client and caretaker can CANCEL")
  void cancelPermission() {
    assertThat(OrderStatusMachine.isAuthorized(OrderStatus.CANCELLED, Role.CLIENT)).isTrue();
    assertThat(OrderStatusMachine.isAuthorized(OrderStatus.CANCELLED, Role.CARETAKER)).isTrue();
  }
}

