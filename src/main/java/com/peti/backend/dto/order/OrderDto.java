package com.peti.backend.dto.order;

import com.peti.backend.dto.PriceItem;
import com.peti.backend.model.domain.Order;
import com.peti.backend.model.internal.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Order details")
public record OrderDto(
    @Schema(description = "Unique order identifier")
    UUID orderId,
    @Schema(description = "Client (pet owner) user ID")
    UUID clientId,
    @Schema(description = "Caretaker ID")
    UUID caretakerId,
    @Schema(description = "Associated event ID")
    UUID eventId,
    @Schema(description = "Total price of the order")
    BigDecimal price,
    @Schema(description = "Currency code (e.g. UAH)")
    String currency,
    @Schema(description = "Detailed price breakdown")
    List<PriceItem> priceBreakdown,
    @Schema(description = "Current order status")
    OrderStatus status,
    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt) {

  public static OrderDto from(Order order) {
    List<PriceItem> breakdown = order.getEvent() != null && order.getEvent().getPrice() != null
        ? order.getEvent().getPrice().priceBreakdown()
        : List.of();
    return new OrderDto(
        order.getOrderId(),
        order.getClient().getUserId(),
        order.getCaretaker().getCaretakerId(),
        order.getEvent().getEventId(),
        order.getPrice(),
        order.getCurrency(),
        breakdown,
        order.getStatus(),
        order.getUpdatedAt()
    );
  }
}

