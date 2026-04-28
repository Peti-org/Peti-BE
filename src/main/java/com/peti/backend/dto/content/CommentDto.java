package com.peti.backend.dto.content;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID userId,
    String userName,
    String content,
    UUID parentCommentId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    long reactions,
    boolean userReacted,
    List<CommentDto> replies
) {
}

