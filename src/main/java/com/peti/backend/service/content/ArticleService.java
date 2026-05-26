package com.peti.backend.service.content;

import com.peti.backend.dto.content.ArticleDto;
import com.peti.backend.dto.content.CursorPageResponse;
import com.peti.backend.dto.content.RequestArticleDto;
import com.peti.backend.model.domain.Article;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.repository.ArticleRepository;
import com.peti.backend.repository.CommentRepository;
import com.peti.backend.repository.ReactionRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {

  private final static int SPEED_OF_READING = 200; // words per minute

  private final ArticleRepository articleRepository;
  private final ReactionRepository reactionRepository;
  private final CommentRepository commentRepository;
  private final EntityManager entityManager;

  public CursorPageResponse<ArticleDto> getALLArticleFeed(int cursor, int limit, @Nullable UUID currentUserId) {
    Page<Article> page = articleRepository.findAllByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(cursor, limit));

    return preparePageResponse(page, cursor, currentUserId);
  }

  public CursorPageResponse<ArticleDto> getArticlesByAuthor(UUID userId, int cursor, int limit,
      @Nullable UUID currentUserId) {
    Page<Article> page = articleRepository.findAllByDeletedFalseAndAuthor_UserIdOrderByCreatedAtDesc(userId,
        PageRequest.of(cursor, limit));

    return preparePageResponse(page, cursor, currentUserId);
  }

  public ArticleDto getArticleById(UUID articleId, UUID currentUserId) {
    Article article = articleRepository.findById(articleId)
        .filter(a -> !a.isDeleted())
        .orElseThrow(() -> new NotFoundException("Article not found: " + articleId));
    return enrichToDto(article, currentUserId);
  }

  @Transactional
  public ArticleDto createArticle(RequestArticleDto request, UUID userId) {
    Article article = new Article();
    article.setTitle(request.title());
    article.setSummary(request.summary());
    article.setContent(request.content());
    article.setTags(request.tags() != null ? request.tags() : List.of());
    article.setCreatedAt(LocalDateTime.now());
    article.setEstimatedReadMinutes(estimateReadMinutes(request.content()));
    article.setAuthor(entityManager.getReference(User.class, userId));
    article.setDeleted(false);

    return enrichToDto(articleRepository.save(article), userId);
  }

  private CursorPageResponse<ArticleDto> preparePageResponse(Page<Article> page, int cursor,
      @Nullable UUID currentUserId) {
    List<ArticleDto> articles = page.getContent().stream()
        .map(a -> enrichToDto(a, currentUserId))
        .toList();

    int nextCursor = page.hasNext() ? cursor + 1 : -1;
    return new CursorPageResponse<>(articles, nextCursor, page.getTotalElements());
  }

  private ArticleDto enrichToDto(Article article, @Nullable UUID currentUserId) {
    long reactions = reactionRepository.countByTargetTypeAndTargetId("article", article.getArticleId());
    long comments = commentRepository.countByTargetTypeAndTargetIdAndDeletedFalse("article", article.getArticleId());
    boolean userReacted = currentUserId != null
        && reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(currentUserId, "article",
        article.getArticleId());

    return toDto(article, reactions, comments, userReacted);
  }

  private ArticleDto toDto(Article article, long reactions, long comments, boolean userReacted) {
    User author = article.getAuthor();
    return new ArticleDto(
        article.getArticleId(),
        article.getTitle(),
        article.getSummary(),
        article.getContent(),
        article.getTags(),
        article.getCreatedAt(),
        article.getUpdatedAt(),
        article.getEstimatedReadMinutes(),
        author.getUserId(),
        author.getFirstName(),
        author.getLastName(),
        reactions,
        comments,
        userReacted
    );
  }

  private int estimateReadMinutes(String content) {
    if (content == null || content.isBlank()) {
      return 1;
    }
    int words = content.split("\\s+").length;
    return Math.max(1, words / SPEED_OF_READING);
  }
}

