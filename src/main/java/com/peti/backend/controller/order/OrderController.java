package com.peti.backend.controller.order;

import com.peti.backend.dto.order.OrderDto;
import com.peti.backend.dto.order.OrderModificationDto;
import com.peti.backend.dto.order.RequestOrderTransitionDto;
import com.peti.backend.model.internal.OrderStatus;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.HasCaretakerRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.order.OrderService;
import com.peti.backend.service.user.CaretakerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management — status transitions, payment lifecycle")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

  private final OrderService orderService;
  private final CaretakerService caretakerService;

  @HasCaretakerRole
  @PostMapping("/from-event/{eventId}")
  @Operation(summary = "Reserve order from approved event",
      description = "Caretaker approves an event and creates an order in RESERVED status "
          + "in a single atomic action. Client is then expected to pay.")
  @ApiResponse(responseCode = "200", description = "Order reserved")
  @ApiResponse(responseCode = "400", description = "Event invalid or order already exists")
  @ApiResponse(responseCode = "404", description = "Event not found")
  public ResponseEntity<OrderDto> reserveFromEvent(
      @PathVariable UUID eventId,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection user) {
    UUID caretakerId = resolveCaretakerId(user);
    return ResponseEntity.ok(orderService.reserveFromEvent(eventId, caretakerId));
  }

  @HasUserRole
  @GetMapping("/{orderId}")
  @Operation(summary = "Get order by ID (only if requester is participant)",
      description = "Returns order details only if the requesting user is the client "
          + "or caretaker of the order. Returns 404 otherwise.")
  @ApiResponse(responseCode = "200", description = "Order found")
  @ApiResponse(responseCode = "404", description = "Order not found or not visible to user")
  public ResponseEntity<OrderDto> getOrder(
      @PathVariable UUID orderId,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection user) {
    UUID actorId = resolveActorId(user);
    return ResponseEntity.ok(orderService.getOrderForActor(orderId, actorId));
  }

  @HasUserRole
  @GetMapping("/{orderId}/modifications")
  @Operation(summary = "Get order modification history",
      description = "Returns the status change history. Only available to the client "
          + "or caretaker of the order.")
  public ResponseEntity<List<OrderModificationDto>> getModifications(
      @PathVariable UUID orderId,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection user) {
    UUID actorId = resolveActorId(user);
    return ResponseEntity.ok(orderService.getModificationsForActor(orderId, actorId));
  }

  @HasUserRole
  @GetMapping("/my")
  @Operation(summary = "Get current user's orders as a client")
  public ResponseEntity<List<OrderDto>> getMyOrders(
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection user) {
    return ResponseEntity.ok(orderService.getOrdersByClientId(user.getUserId()));
  }

  @HasCaretakerRole
  @GetMapping("/caretaker/my")
  @Operation(summary = "Get current caretaker's orders")
  public ResponseEntity<List<OrderDto>> getCaretakerOrders(
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection user) {
    UUID caretakerId = resolveCaretakerId(user);
    return ResponseEntity.ok(orderService.getOrdersByCaretakerId(caretakerId));
  }

  @HasCaretakerRole
  @PostMapping("/{orderId}/decline")
  @Operation(summary = "Caretaker declines the order (RESERVED → DECLINED)")
  public ResponseEntity<OrderDto> decline(
      @PathVariable UUID orderId,
      @Valid @RequestBody(required = false) RequestOrderTransitionDto dto,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection user) {
    UUID caretakerId = resolveCaretakerId(user);
    String comment = dto != null ? dto.comment() : null;
    return ResponseEntity.ok(
        orderService.transition(orderId, OrderStatus.DECLINED, caretakerId, comment));
  }

  @HasUserRole
  @PostMapping("/{orderId}/pay")
  @Operation(summary = "Client pays for order (RESERVED → DEFERRED_PAYMENT)")
  public ResponseEntity<OrderDto> pay(
      @PathVariable UUID orderId,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection user) {
    return ResponseEntity.ok(
        orderService.transition(orderId, OrderStatus.DEFERRED_PAYMENT, user.getUserId(), null));
  }

  @HasUserRole
  @PostMapping("/{orderId}/cancel")
  @Operation(summary = "Cancel order (RESERVED/DEFERRED_PAYMENT → CANCELLED)")
  public ResponseEntity<OrderDto> cancel(
      @PathVariable UUID orderId,
      @Valid @RequestBody(required = false) RequestOrderTransitionDto dto,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection user) {
    String comment = dto != null ? dto.comment() : null;
    UUID actorId = resolveActorId(user);
    return ResponseEntity.ok(
        orderService.transition(orderId, OrderStatus.CANCELLED, actorId, comment));
  }

  @ModelAttribute("userProjection")
  public UserProjection getUserProjection(Authentication authentication) {
    return (UserProjection) authentication.getPrincipal();
  }

  /**
   * Resolves the actor identifier used by the order status machine.
   * For caretaker users we use their caretakerId; otherwise we use the userId.
   */
  private UUID resolveActorId(UserProjection user) {
    return caretakerService.getCaretakerIdByUserId(user.getUserId())
        .orElse(user.getUserId());
  }

  private UUID resolveCaretakerId(UserProjection user) {
    return caretakerService.getCaretakerIdByUserId(user.getUserId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Caretaker not found for user: " + user.getUserId()));
  }
}

