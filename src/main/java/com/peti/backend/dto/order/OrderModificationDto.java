package com.peti.backend.dto.order;

import com.peti.backend.model.domain.OrderModification;
import com.peti.backend.model.internal.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "A single status change record in an order's history")
public record OrderModificationDto(
    @Schema(description = "Modification ID")
    UUID modificationId,
    @Schema(description = "Order ID")
    UUID orderId,
    @Schema(description = "Status that was set")
    OrderStatus status,
    @Schema(description = "Timestamp of the status change")
    LocalDateTime createdAt,
    @Schema(description = "User who triggered the change (ID only for non-admin views)")
    UUID actorId,
    @Schema(description = "Detailed actor info (admin only)")
    UserInfoDto actor,
    @Schema(description = "Optional comment")
    String comment) {

  public static OrderModificationDto from(OrderModification mod) {
    return new OrderModificationDto(
        mod.getModificationId(),
        mod.getOrder() != null ? mod.getOrder().getOrderId() : null,
        mod.getStatus(),
        mod.getCreatedAt(),
        mod.getActorId(),
        null,
        mod.getComment()
    );
  }

  public static OrderModificationDto fromAdmin(OrderModification mod, UserInfoDto actor) {
    return new OrderModificationDto(
        mod.getModificationId(),
        mod.getOrder() != null ? mod.getOrder().getOrderId() : null,
        mod.getStatus(),
        mod.getCreatedAt(),
        mod.getActorId(),
        actor,
        mod.getComment()
    );
  }
}

