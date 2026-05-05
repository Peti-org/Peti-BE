package com.peti.backend.controller.order;

import com.peti.backend.dto.order.OrderDto;
import com.peti.backend.dto.order.OrderModificationDto;
import com.peti.backend.dto.order.RequestOrderTransitionDto;
import com.peti.backend.model.internal.OrderStatus;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.CurrentCaretakerId;
import com.peti.backend.security.annotation.CurrentUser;
import com.peti.backend.security.annotation.HasCaretakerRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.order.OrderService;
import com.peti.backend.service.order.OrderStatusMachine.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

  @HasCaretakerRole
  @PostMapping("/caretaker/from-event/{eventId}")
  @Operation(summary = "Caretaker reserves an order from an approved event",
      description = "Creates an order in RESERVED status. Client is then expected to pay.")
  @ApiResponse(responseCode = "200", description = "Order reserved")
  @ApiResponse(responseCode = "400", description = "Event invalid or order already exists")
  @ApiResponse(responseCode = "404", description = "Event not found")
  public ResponseEntity<OrderDto> reserveFromEvent(
      @PathVariable UUID eventId,
      @CurrentCaretakerId UUID caretakerId) {
    return ResponseEntity.ok(orderService.reserveFromEvent(eventId, caretakerId));
  }

  @HasCaretakerRole
  @GetMapping("/caretaker/my")
  @Operation(summary = "List all orders for the current caretaker")
  public ResponseEntity<List<OrderDto>> getCaretakerOrders(@CurrentCaretakerId UUID caretakerId) {
    return ResponseEntity.ok(orderService.getOrdersByCaretakerId(caretakerId));
  }

  @HasCaretakerRole
  @GetMapping("/caretaker/{orderId}")
  @Operation(summary = "Caretaker view of a specific order")
  @ApiResponse(responseCode = "404", description = "Order not found or not owned by this caretaker")
  public ResponseEntity<OrderDto> getOrderAsCaretaker(
      @PathVariable UUID orderId,
      @CurrentCaretakerId UUID caretakerId) {
    return ResponseEntity.ok(orderService.getOrderAsCaretaker(orderId, caretakerId));
  }

  @HasCaretakerRole
  @GetMapping("/caretaker/{orderId}/modifications")
  @Operation(summary = "Order modification history — caretaker view")
  public ResponseEntity<List<OrderModificationDto>> getModificationsAsCaretaker(
      @PathVariable UUID orderId,
      @CurrentCaretakerId UUID caretakerId) {
    return ResponseEntity.ok(orderService.getModificationsAsCaretaker(orderId, caretakerId));
  }

  @HasCaretakerRole
  @PostMapping("/caretaker/{orderId}/decline")
  @Operation(summary = "Caretaker declines the order (RESERVED → DECLINED)")
  public ResponseEntity<OrderDto> decline(
      @PathVariable UUID orderId,
      @Valid @RequestBody(required = false) RequestOrderTransitionDto dto,
      @CurrentCaretakerId UUID caretakerId) {
    String comment = dto != null ? dto.comment() : null;
    return ResponseEntity.ok(
        orderService.transition(orderId, OrderStatus.DECLINED, caretakerId, Role.CARETAKER, comment));
  }

  //todo same method as previos think if can be deleted
  @HasCaretakerRole
  @PostMapping("/caretaker/{orderId}/cancel")
  @Operation(summary = "Caretaker cancels the order (RESERVED → CANCELLED)")
  public ResponseEntity<OrderDto> cancelAsCaretaker(
      @PathVariable UUID orderId,
      @Valid @RequestBody(required = false) RequestOrderTransitionDto dto,
      @CurrentCaretakerId UUID caretakerId) {
    String comment = dto != null ? dto.comment() : null;
    return ResponseEntity.ok(
        orderService.transition(orderId, OrderStatus.CANCELLED, caretakerId, Role.CARETAKER, comment));
  }

  @HasUserRole
  @GetMapping("/client/my")
  @Operation(summary = "List all orders for the current client")
  public ResponseEntity<List<OrderDto>> getClientOrders(@CurrentUser UserProjection user) {
    return ResponseEntity.ok(orderService.getOrdersByClientId(user.getUserId()));
  }

  @HasUserRole
  @GetMapping("/client/{orderId}")
  @Operation(summary = "Client view of a specific order")
  @ApiResponse(responseCode = "404", description = "Order not found or not owned by this client")
  public ResponseEntity<OrderDto> getOrderAsClient(
      @PathVariable UUID orderId,
      @CurrentUser UserProjection user) {
    return ResponseEntity.ok(orderService.getOrderAsClient(orderId, user.getUserId()));
  }

  @HasUserRole
  @GetMapping("/client/{orderId}/modifications")
  @Operation(summary = "Order modification history — client view")
  public ResponseEntity<List<OrderModificationDto>> getModificationsAsClient(
      @PathVariable UUID orderId,
      @CurrentUser UserProjection user) {
    return ResponseEntity.ok(orderService.getModificationsAsClient(orderId, user.getUserId()));
  }

  @HasUserRole
  @PostMapping("/client/{orderId}/pay")
  @Operation(summary = "Client pays for the order (RESERVED → DEFERRED_PAYMENT)")
  public ResponseEntity<OrderDto> pay(
      @PathVariable UUID orderId,
      @CurrentUser UserProjection user) {
    return ResponseEntity.ok(
        orderService.transition(orderId, OrderStatus.DEFERRED_PAYMENT, user.getUserId(), Role.CLIENT, null));
  }

  @HasUserRole
  @PostMapping("/client/{orderId}/cancel")
  @Operation(summary = "Client cancels the order (RESERVED/DEFERRED_PAYMENT → CANCELLED)")
  public ResponseEntity<OrderDto> cancelAsClient(
      @PathVariable UUID orderId,
      @Valid @RequestBody(required = false) RequestOrderTransitionDto dto,
      @CurrentUser UserProjection user) {
    String comment = dto != null ? dto.comment() : null;
    return ResponseEntity.ok(
        orderService.transition(orderId, OrderStatus.CANCELLED, user.getUserId(), Role.CLIENT, comment));
  }
}

