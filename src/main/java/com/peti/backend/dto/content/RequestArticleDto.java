package com.peti.backend.dto.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RequestArticleDto(
    @NotBlank(message = "Title must not be blank")
    @Size(max = 300, message = "Title must not exceed 300 characters")
    String title,

    @Size(max = 1000, message = "Summary must not exceed 1000 characters")
    String summary,

    @NotBlank(message = "Content must not be blank")
    String content,

    List<String> tags
) {
}

