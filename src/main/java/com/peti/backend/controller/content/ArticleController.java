package com.peti.backend.controller.content;

import com.peti.backend.dto.content.ArticleDto;
import com.peti.backend.dto.content.CursorPageResponse;
import com.peti.backend.dto.content.RequestArticleDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.CurrentUser;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.content.ArticleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@Tag(name = "Articles", description = "Community articles and blog posts")
@SecurityRequirement(name = "bearerAuth")
public class ArticleController {

  private final ArticleService articleService;

  @GetMapping
  public ResponseEntity<CursorPageResponse<ArticleDto>> getArticleFeed(
      @RequestParam(defaultValue = "0") int cursor,
      @RequestParam(defaultValue = "5") int limit,
      @CurrentUser UserProjection userProjection) {
    return ResponseEntity.ok(articleService.getArticleFeed(cursor, limit, userProjection.getUserId()));
  }

  @GetMapping("/{articleId}")
  public ResponseEntity<ArticleDto> getArticleById(
      @PathVariable UUID articleId,
      @CurrentUser UserProjection userProjection) {
    return ResponseEntity.ok(articleService.getArticleById(articleId, userProjection.getUserId()));
  }

  @GetMapping("/authors/{userId}/articles")
  public ResponseEntity<CursorPageResponse<ArticleDto>> getArticlesByAuthor(
      @PathVariable UUID userId,
      @RequestParam(defaultValue = "0") int cursor,
      @RequestParam(defaultValue = "5") int limit,
      @CurrentUser UserProjection userProjection) {
    return ResponseEntity.ok(articleService.getArticlesByAuthor(userId, cursor, limit, userProjection.getUserId()));
  }

  @HasUserRole
  @PostMapping
  public ResponseEntity<ArticleDto> createArticle(
      @Valid @RequestBody RequestArticleDto request,
      @CurrentUser UserProjection userProjection) {
    return ResponseEntity.ok(articleService.createArticle(request, userProjection.getUserId()));
  }
}
