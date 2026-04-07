package com.peti.backend.dto.elastic;

import java.util.List;


public record ElasticSlotSearchResponse(
    List<SlotSearchResult> slots,
    int page,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
  public static ElasticSlotSearchResponse of(
      List<SlotSearchResult> slots,
      int page,
      int pageSize,
      long totalElements
  ) {
    int totalPages = (int) Math.ceil((double) totalElements / pageSize);
    return new ElasticSlotSearchResponse(
        slots,
        page,
        pageSize,
        totalElements,
        totalPages,
        page < totalPages - 1,
        page > 0
    );
  }
}
