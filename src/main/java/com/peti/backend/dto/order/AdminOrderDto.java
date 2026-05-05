package com.peti.backend.dto.order;

import com.peti.backend.dto.PriceItem;
import com.peti.backend.model.domain.Order;
import com.peti.backend.model.internal.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Order details for admin view (includes deleted flag and user info)")
public record AdminOrderDto(
    UUID orderId,
    UserInfoDto client,
    UUID caretakerId,
    UUID eventId,
    BigDecimal price,
    String currency,
    List<PriceItem> priceBreakdown,
    OrderStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean deleted) {

  public static AdminOrderDto from(Order order) {
    List<PriceItem> breakdown = order.getEvent() != null && order.getEvent().getPrice() != null
        ? order.getEvent().getPrice().priceBreakdown()
        : List.of();
    return new AdminOrderDto(
        order.getOrderId(),
        UserInfoDto.from(order.getClient()),
        order.getCaretaker().getCaretakerId(),
        order.getEvent().getEventId(),
        order.getPrice(),
        order.getCurrency(),
        breakdown,
        order.getStatus(),
        order.getCreatedAt(),
        order.getUpdatedAt(),
        order.isDeleted()
    );
  }
}

