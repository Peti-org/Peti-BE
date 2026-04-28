package com.peti.backend.dto.content;

import java.util.List;

public record CursorPageResponse<T>(
    List<T> items,
    int nextCursor,
    long total
) {
}

