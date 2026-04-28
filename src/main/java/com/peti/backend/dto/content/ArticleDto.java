package com.peti.backend.dto.content;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleDto(
    UUID id,
    String title,
    String summary,
    String content,
    List<String> tags,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    int estimatedReadMinutes,
    UUID authorId,
    String authorFirstName,
    String authorLastName,
    long reactions,
    long comments,
    boolean userReacted
) {
}

