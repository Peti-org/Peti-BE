package com.peti.backend.dto;

import java.util.List;
import java.math.BigDecimal;

public record PriceDto(BigDecimal totalPrice, String currency, List<PriceItem> priceBreakdown) {

}
