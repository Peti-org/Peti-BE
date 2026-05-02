package com.peti.backend.service.order;

import com.peti.backend.model.internal.OrderStatus;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Single source of truth for order status transitions and permissions. Stateless utility — all methods are static.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderStatusMachine {

  private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
      OrderStatus.RESERVED, Set.of(OrderStatus.DEFERRED_PAYMENT, OrderStatus.DECLINED, OrderStatus.CANCELLED),
      OrderStatus.DEFERRED_PAYMENT, Set.of(OrderStatus.EXECUTING, OrderStatus.CANCELLED),
      OrderStatus.EXECUTING, Set.of(OrderStatus.FINISHED),
      OrderStatus.FINISHED, Set.of(OrderStatus.PAID)
  );

  private static final Map<OrderStatus, Set<Role>> PERMISSIONS = Map.of(
      OrderStatus.DECLINED, Set.of(Role.CARETAKER),
      OrderStatus.DEFERRED_PAYMENT, Set.of(Role.CLIENT),
      OrderStatus.CANCELLED, Set.of(Role.CLIENT, Role.CARETAKER),
      OrderStatus.EXECUTING, Set.of(Role.CLIENT, Role.CARETAKER),
      OrderStatus.FINISHED, Set.of(Role.CLIENT, Role.CARETAKER),
      OrderStatus.PAID, Set.of(Role.CLIENT, Role.CARETAKER)
  );

  public static boolean canTransition(OrderStatus from, OrderStatus to) {
    Set<OrderStatus> allowed = TRANSITIONS.get(from);
    return allowed != null && allowed.contains(to);
  }

  public static boolean isTerminal(OrderStatus status) {
    return status == OrderStatus.PAID
        || status == OrderStatus.DECLINED
        || status == OrderStatus.CANCELLED;
  }

  public static boolean isAuthorized(OrderStatus target, Role actorRole) {
    if (actorRole == null) {
      return false;
    }
    Set<Role> allowed = PERMISSIONS.get(target);
    return allowed != null && allowed.contains(actorRole);
  }

  /**
   * Roles permitted to trigger a transition into a given target status.
   */
  public enum Role {CLIENT, CARETAKER}
}

