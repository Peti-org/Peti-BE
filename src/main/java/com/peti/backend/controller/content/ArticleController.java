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

  private static final UUID ADMIN_NEWS_ID = UUID.fromString("7dc587a7-9ab7-472b-b3b6-6702c4f8a680");
  private static final String DEFAULT_CURSOR = "0";
  private static final String DEFAULT_LIMIT = "5";

  private final ArticleService articleService;

  @GetMapping
  public ResponseEntity<CursorPageResponse<ArticleDto>> getAllArticlesFeed(
      @RequestParam(defaultValue = DEFAULT_CURSOR) int cursor,
      @RequestParam(defaultValue = DEFAULT_LIMIT) int limit,
      @CurrentUser UserProjection userProjection) {
    UUID currentUserId = userProjection != null ? userProjection.getUserId() : null;
    return ResponseEntity.ok(articleService.getALLArticleFeed(cursor, limit, currentUserId));
  }

  @HasUserRole
  @GetMapping("/my")
  public ResponseEntity<CursorPageResponse<ArticleDto>> getMyArticlesFeed(
      @RequestParam(defaultValue = DEFAULT_CURSOR) int cursor,
      @RequestParam(defaultValue = DEFAULT_LIMIT) int limit,
      @CurrentUser UserProjection userProjection) {
    return ResponseEntity.ok(articleService.getArticlesByAuthor(userProjection.getUserId(), cursor, limit, userProjection.getUserId()));
  }

  @GetMapping("/authors/{userId}")
  public ResponseEntity<CursorPageResponse<ArticleDto>> getArticlesByAuthor(
      @PathVariable UUID userId,
      @RequestParam(defaultValue = DEFAULT_CURSOR) int cursor,
      @RequestParam(defaultValue = DEFAULT_LIMIT) int limit,
      @CurrentUser UserProjection userProjection) {
    UUID currentUserId = userProjection != null ? userProjection.getUserId() : null;
    return ResponseEntity.ok(articleService.getArticlesByAuthor(userId, cursor, limit, currentUserId));
  }

  @GetMapping("/news")
  public ResponseEntity<CursorPageResponse<ArticleDto>> getNews(
      @RequestParam(defaultValue = DEFAULT_CURSOR) int cursor,
      @RequestParam(defaultValue = DEFAULT_LIMIT) int limit,
      @CurrentUser UserProjection userProjection) {
    UUID currentUserId = userProjection != null ? userProjection.getUserId() : null;
    return ResponseEntity.ok(articleService.getArticlesByAuthor(ADMIN_NEWS_ID, cursor, limit, currentUserId));
  }

  @GetMapping("/{articleId}")
  public ResponseEntity<ArticleDto> getArticleById(
      @PathVariable UUID articleId,
      @CurrentUser UserProjection userProjection) {
    UUID currentUserId = userProjection != null ? userProjection.getUserId() : null;
    return ResponseEntity.ok(articleService.getArticleById(articleId, currentUserId));
  }

  @HasUserRole
  @PostMapping
  public ResponseEntity<ArticleDto> createArticle(
      @Valid @RequestBody RequestArticleDto request,
      @CurrentUser UserProjection userProjection) {
    return ResponseEntity.ok(articleService.createArticle(request, userProjection.getUserId()));
  }
}
