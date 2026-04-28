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

import com.peti.backend.dto.content.CommentDto;
import com.peti.backend.dto.content.RequestCommentDto;
import com.peti.backend.dto.exception.NotFoundException;
import com.peti.backend.model.domain.Comment;
import com.peti.backend.model.domain.User;
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

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;
  @Mock
  private ReactionRepository reactionRepository;
  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private CommentService commentService;

  private UUID userId;
  private User user;
  private Comment comment;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = new User(userId);
    user.setFirstName("Alice");

    comment = new Comment();
    comment.setCommentId(UUID.randomUUID());
    comment.setUser(user);
    comment.setContent("Great article!");
    comment.setTargetType("article");
    comment.setTargetId(UUID.randomUUID());
    comment.setCreatedAt(LocalDateTime.now());
    comment.setDeleted(false);
  }

  @Test
  void getComments_returnsWithUserReacted() {
    when(commentRepository.findAllByTargetTypeAndTargetIdAndParentCommentIsNullAndDeletedFalseOrderByCreatedAtAsc(
        "article", comment.getTargetId()))
        .thenReturn(List.of(comment));
    when(commentRepository.findAllByParentComment_CommentIdAndDeletedFalseOrderByCreatedAtAsc(comment.getCommentId()))
        .thenReturn(List.of());
    when(reactionRepository.countByTargetTypeAndTargetId("comment", comment.getCommentId()))
        .thenReturn(2L);
    when(reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(eq(userId), eq("comment"), eq(comment.getCommentId())))
        .thenReturn(true);

    List<CommentDto> result = commentService.getComments("article", comment.getTargetId(), userId);

    assertEquals(1, result.size());
    assertTrue(result.get(0).userReacted());
    assertEquals(2, result.get(0).reactions());
  }

  @Test
  void getComments_userNotReacted_returnsFalse() {
    when(commentRepository.findAllByTargetTypeAndTargetIdAndParentCommentIsNullAndDeletedFalseOrderByCreatedAtAsc(
        "article", comment.getTargetId()))
        .thenReturn(List.of(comment));
    when(commentRepository.findAllByParentComment_CommentIdAndDeletedFalseOrderByCreatedAtAsc(comment.getCommentId()))
        .thenReturn(List.of());
    when(reactionRepository.countByTargetTypeAndTargetId("comment", comment.getCommentId()))
        .thenReturn(0L);
    when(reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(eq(userId), eq("comment"), eq(comment.getCommentId())))
        .thenReturn(false);

    List<CommentDto> result = commentService.getComments("article", comment.getTargetId(), userId);

    assertFalse(result.get(0).userReacted());
  }

  @Test
  void getComments_nullUserId_returnsFalseForUserReacted() {
    when(commentRepository.findAllByTargetTypeAndTargetIdAndParentCommentIsNullAndDeletedFalseOrderByCreatedAtAsc(
        "article", comment.getTargetId()))
        .thenReturn(List.of(comment));
    when(commentRepository.findAllByParentComment_CommentIdAndDeletedFalseOrderByCreatedAtAsc(comment.getCommentId()))
        .thenReturn(List.of());
    when(reactionRepository.countByTargetTypeAndTargetId("comment", comment.getCommentId()))
        .thenReturn(1L);

    List<CommentDto> result = commentService.getComments("article", comment.getTargetId(), null);

    assertFalse(result.get(0).userReacted());
  }

  @Test
  void createComment_withoutParent_savesSuccessfully() {
    when(entityManager.getReference(User.class, userId)).thenReturn(user);
    when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
      Comment saved = inv.getArgument(0);
      saved.setCommentId(UUID.randomUUID());
      saved.setUser(user);
      return saved;
    });
    when(reactionRepository.countByTargetTypeAndTargetId(any(), any())).thenReturn(0L);

    RequestCommentDto request = new RequestCommentDto("Nice!", "article", UUID.randomUUID(), null);
    CommentDto result = commentService.createComment(request, userId);

    assertNotNull(result);
    assertEquals("Nice!", result.content());
    assertFalse(result.userReacted());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void createComment_withParent_linksParent() {
    Comment parent = new Comment();
    parent.setCommentId(UUID.randomUUID());

    when(entityManager.getReference(User.class, userId)).thenReturn(user);
    when(commentRepository.findById(parent.getCommentId())).thenReturn(Optional.of(parent));
    when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
      Comment saved = inv.getArgument(0);
      saved.setCommentId(UUID.randomUUID());
      saved.setUser(user);
      return saved;
    });
    when(reactionRepository.countByTargetTypeAndTargetId(any(), any())).thenReturn(0L);

    RequestCommentDto request = new RequestCommentDto("Reply!", "article", UUID.randomUUID(), parent.getCommentId());
    CommentDto result = commentService.createComment(request, userId);

    assertNotNull(result);
    assertEquals(parent.getCommentId(), result.parentCommentId());
  }

  @Test
  void createComment_parentNotFound_throwsNotFound() {
    UUID parentId = UUID.randomUUID();
    when(entityManager.getReference(User.class, userId)).thenReturn(user);
    when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

    RequestCommentDto request = new RequestCommentDto("Reply!", "article", UUID.randomUUID(), parentId);

    assertThrows(NotFoundException.class, () -> commentService.createComment(request, userId));
  }

  @Test
  void deleteComment_ownComment_setsDeleted() {
    when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

    commentService.deleteComment(comment.getCommentId(), userId);

    verify(commentRepository).save(comment);
    assertTrue(comment.isDeleted());
  }

  @Test
  void deleteComment_otherUserComment_throwsException() {
    UUID otherUserId = UUID.randomUUID();
    when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

    assertThrows(IllegalArgumentException.class,
        () -> commentService.deleteComment(comment.getCommentId(), otherUserId));
  }

  @Test
  void deleteComment_notFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    when(commentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> commentService.deleteComment(id, userId));
  }
}

