package com.peti.backend.service.order;

import com.peti.backend.model.internal.OrderStatus;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Single source of truth for order status transitions and permissions.
 * Stateless utility — all methods are static.
 */
public final class OrderStatusMachine {

  /** Roles permitted to trigger a transition into a given target status. */
  public enum Role { CLIENT, CARETAKER }

  private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
      OrderStatus.RESERVED, Set.of(
          OrderStatus.DEFERRED_PAYMENT, OrderStatus.DECLINED, OrderStatus.CANCELLED),
      OrderStatus.DEFERRED_PAYMENT, Set.of(
          OrderStatus.EXECUTING, OrderStatus.CANCELLED),
      OrderStatus.EXECUTING, Set.of(
          OrderStatus.FINISHED),
      OrderStatus.FINISHED, Set.of(
          OrderStatus.PAID)
  );

  /** Maps each target status to the set of roles allowed to trigger it. */
  private static final Map<OrderStatus, Set<Role>> PERMISSIONS = Map.of(
      OrderStatus.DECLINED, Set.of(Role.CARETAKER),
      OrderStatus.DEFERRED_PAYMENT, Set.of(Role.CLIENT),
      OrderStatus.CANCELLED, Set.of(Role.CLIENT, Role.CARETAKER),
      OrderStatus.EXECUTING, Set.of(Role.CLIENT, Role.CARETAKER),
      OrderStatus.FINISHED, Set.of(Role.CLIENT, Role.CARETAKER),
      OrderStatus.PAID, Set.of(Role.CLIENT, Role.CARETAKER)
  );

  private OrderStatusMachine() {
  }

  public static boolean canTransition(OrderStatus from, OrderStatus to) {
    Set<OrderStatus> allowed = TRANSITIONS.get(from);
    return allowed != null && allowed.contains(to);
  }

  public static Set<OrderStatus> allowedTransitions(OrderStatus from) {
    return TRANSITIONS.getOrDefault(from, Set.of());
  }

  public static boolean isTerminal(OrderStatus status) {
    return status == OrderStatus.PAID
        || status == OrderStatus.DECLINED
        || status == OrderStatus.CANCELLED;
  }

  /**
   * Resolves the role of an actor relative to the given order parties.
   * @return the actor's role, or {@code null} if the actor is neither client nor caretaker.
   */
  public static Role resolveRole(UUID actorId, UUID clientId, UUID caretakerId) {
    if (clientId.equals(actorId)) {
      return Role.CLIENT;
    }
    if (caretakerId.equals(actorId)) {
      return Role.CARETAKER;
    }
    return null;
  }

  public static boolean isAuthorized(OrderStatus target, Role actorRole) {
    if (actorRole == null) {
      return false;
    }
    Set<Role> allowed = PERMISSIONS.get(target);
    return allowed != null && allowed.contains(actorRole);
  }

  /** Convenience: actor is authorized to view the order if they are client or caretaker. */
  public static boolean canView(UUID actorId, UUID clientId, UUID caretakerId) {
    return resolveRole(actorId, clientId, caretakerId) != null;
  }
}

