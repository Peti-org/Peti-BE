package com.peti.backend.service.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.dto.content.ArticleDto;
import com.peti.backend.dto.content.CursorPageResponse;
import com.peti.backend.dto.content.RequestArticleDto;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.domain.Article;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.ArticleRepository;
import com.peti.backend.repository.CommentRepository;
import com.peti.backend.repository.ReactionRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

  @Mock
  private ArticleRepository articleRepository;
  @Mock
  private ReactionRepository reactionRepository;
  @Mock
  private CommentRepository commentRepository;
  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private ArticleService articleService;

  private User author;
  private Article article;
  private UUID currentUserId;

  @BeforeEach
  void setUp() {
    author = new User(UUID.randomUUID());
    author.setFirstName("Alice");
    author.setLastName("Smith");
    currentUserId = UUID.randomUUID();

    article = new Article();
    article.setArticleId(UUID.randomUUID());
    article.setTitle("Test Article");
    article.setSummary("Summary");
    article.setContent("Article content");
    article.setTags(List.of("dogs"));
    article.setCreatedAt(LocalDateTime.now());
    article.setEstimatedReadMinutes(2);
    article.setAuthor(author);
    article.setDeleted(false);
  }

  @Test
  void getArticleFeed_returnsPageWithUserReacted() {
    PageImpl<Article> page = new PageImpl<>(List.of(article), PageRequest.of(0, 5), 1);
    when(articleRepository.findAllByDeletedFalseOrderByCreatedAtDesc(any())).thenReturn(page);
    when(reactionRepository.countByTargetTypeAndTargetId(any(), any())).thenReturn(2L);
    when(commentRepository.countByTargetTypeAndTargetIdAndDeletedFalse(any(), any())).thenReturn(1L);
    when(reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(eq(currentUserId), eq("article"), any()))
        .thenReturn(true);

    CursorPageResponse<ArticleDto> result = articleService.getArticleFeed(0, 5, currentUserId);

    assertEquals(1, result.items().size());
    assertTrue(result.items().get(0).userReacted());
  }

  @Test
  void getArticleFeed_userNotReacted_returnsFalse() {
    PageImpl<Article> page = new PageImpl<>(List.of(article), PageRequest.of(0, 5), 1);
    when(articleRepository.findAllByDeletedFalseOrderByCreatedAtDesc(any())).thenReturn(page);
    when(reactionRepository.countByTargetTypeAndTargetId(any(), any())).thenReturn(2L);
    when(commentRepository.countByTargetTypeAndTargetIdAndDeletedFalse(any(), any())).thenReturn(1L);
    when(reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(eq(currentUserId), eq("article"), any()))
        .thenReturn(false);

    CursorPageResponse<ArticleDto> result = articleService.getArticleFeed(0, 5, currentUserId);

    assertFalse(result.items().get(0).userReacted());
  }

  @Test
  void getArticlesByAuthor_returnsFilteredPage() {
    PageImpl<Article> page = new PageImpl<>(List.of(article), PageRequest.of(0, 5), 1);
    when(articleRepository.findAllByDeletedFalseAndAuthor_UserIdOrderByCreatedAtDesc(eq(author.getUserId()), any()))
        .thenReturn(page);
    when(reactionRepository.countByTargetTypeAndTargetId(any(), any())).thenReturn(0L);
    when(commentRepository.countByTargetTypeAndTargetIdAndDeletedFalse(any(), any())).thenReturn(0L);

    CursorPageResponse<ArticleDto> result = articleService.getArticlesByAuthor(author.getUserId(), 0, 5, null);

    assertEquals(1, result.items().size());
    assertFalse(result.items().get(0).userReacted());
  }

  @Test
  void getArticleById_existing_returnsDto() {
    when(articleRepository.findById(article.getArticleId())).thenReturn(Optional.of(article));
    when(reactionRepository.countByTargetTypeAndTargetId(any(), any())).thenReturn(5L);
    when(commentRepository.countByTargetTypeAndTargetIdAndDeletedFalse(any(), any())).thenReturn(3L);
    when(reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(eq(currentUserId), eq("article"), any()))
        .thenReturn(true);

    ArticleDto result = articleService.getArticleById(article.getArticleId(), currentUserId);

    assertNotNull(result);
    assertEquals(5, result.reactions());
    assertTrue(result.userReacted());
  }

  @Test
  void getArticleById_deleted_throwsNotFound() {
    article.setDeleted(true);
    when(articleRepository.findById(article.getArticleId())).thenReturn(Optional.of(article));

    assertThrows(NotFoundException.class, () -> articleService.getArticleById(article.getArticleId(), currentUserId));
  }

  @Test
  void getArticleById_notExisting_throwsNotFound() {
    UUID id = UUID.randomUUID();
    when(articleRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> articleService.getArticleById(id, currentUserId));
  }

  @Test
  void createArticle_valid_savesAndReturns() {
    when(entityManager.getReference(User.class, author.getUserId())).thenReturn(author);
    when(articleRepository.save(any(Article.class))).thenAnswer(inv -> {
      Article saved = inv.getArgument(0);
      saved.setArticleId(UUID.randomUUID());
      return saved;
    });
    when(reactionRepository.countByTargetTypeAndTargetId(any(), any())).thenReturn(0L);
    when(commentRepository.countByTargetTypeAndTargetIdAndDeletedFalse(any(), any())).thenReturn(0L);

    RequestArticleDto request = new RequestArticleDto("Title", "Summary", "Body content here", List.of("tag"));
    ArticleDto result = articleService.createArticle(request, author.getUserId());

    assertNotNull(result);
    assertEquals("Title", result.title());
    assertFalse(result.userReacted());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void getArticleFeed_emptyResult_returnsEmptyItems() {
    PageImpl<Article> page = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);
    when(articleRepository.findAllByDeletedFalseOrderByCreatedAtDesc(any())).thenReturn(page);

    CursorPageResponse<ArticleDto> result = articleService.getArticleFeed(0, 5, currentUserId);

    assertEquals(0, result.items().size());
    assertEquals(-1, result.nextCursor());
  }
}

