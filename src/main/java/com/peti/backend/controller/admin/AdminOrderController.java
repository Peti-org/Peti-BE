package com.peti.backend.controller.admin;

import com.peti.backend.dto.order.AdminOrderDto;
import com.peti.backend.dto.order.OrderModificationDto;
import com.peti.backend.model.internal.OrderStatus;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.service.order.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints for inspecting orders. Allows filtering by status,
 * date range, caretaker, client, and includes deleted orders + full user info.
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin: Orders", description = "Admin operations for inspecting orders")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

  private final AdminOrderService adminOrderService;

  @HasAdminRole
  @GetMapping
  @Operation(summary = "List orders with filters and pagination",
      description = "All filters are optional. Includes deleted orders.")
  public ResponseEntity<Page<AdminOrderDto>> listOrders(
      @Parameter(description = "Filter by order status")
      @RequestParam(required = false) OrderStatus status,
      @Parameter(description = "Filter by caretaker UUID")
      @RequestParam(required = false) UUID caretakerId,
      @Parameter(description = "Filter by client (user) UUID")
      @RequestParam(required = false) UUID clientId,
      @Parameter(description = "Created at >= this timestamp (ISO-8601)")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @Parameter(description = "Created at <= this timestamp (ISO-8601)")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(adminOrderService.findOrders(
        status, caretakerId, clientId, from, to, pageable));
  }

  @HasAdminRole
  @GetMapping("/{orderId}")
  @Operation(summary = "Get any order by ID (including deleted)")
  public ResponseEntity<AdminOrderDto> getOrder(@PathVariable UUID orderId) {
    return ResponseEntity.ok(adminOrderService.getOrder(orderId));
  }

  @HasAdminRole
  @GetMapping("/{orderId}/modifications")
  @Operation(summary = "Get modifications of any order with full actor user info")
  public ResponseEntity<List<OrderModificationDto>> getModifications(
      @PathVariable UUID orderId) {
    return ResponseEntity.ok(adminOrderService.getModifications(orderId));
  }
}

