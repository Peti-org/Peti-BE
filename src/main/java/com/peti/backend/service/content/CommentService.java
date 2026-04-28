package com.peti.backend.service.content;

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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final ReactionRepository reactionRepository;
  private final EntityManager entityManager;

  public List<CommentDto> getComments(String targetType, UUID targetId, UUID currentUserId) {
    List<Comment> roots = commentRepository
        .findAllByTargetTypeAndTargetIdAndParentCommentIsNullAndDeletedFalseOrderByCreatedAtAsc(
            targetType, targetId);

    return roots.stream()
        .map(c -> toDtoWithReplies(c, currentUserId))
        .toList();
  }

  @Transactional
  public CommentDto createComment(RequestCommentDto request, UUID userId) {
    Comment comment = new Comment();
    comment.setUser(entityManager.getReference(User.class, userId));
    comment.setContent(request.content());
    comment.setTargetType(request.targetType());
    comment.setTargetId(request.targetId());
    comment.setCreatedAt(LocalDateTime.now());
    comment.setDeleted(false);

    if (request.parentCommentId() != null) {
      Comment parent = commentRepository.findById(request.parentCommentId())
          .orElseThrow(() -> new NotFoundException("Parent comment not found"));
      comment.setParentComment(parent);
    }

    Comment saved = commentRepository.save(comment);
    return toDto(saved, userId);
  }

  @Transactional
  public void deleteComment(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new NotFoundException("Comment not found"));
    if (!comment.getUser().getUserId().equals(userId)) {
      throw new IllegalArgumentException("Cannot delete another user's comment");
    }
    comment.setDeleted(true);
    commentRepository.save(comment);
  }

  private CommentDto toDtoWithReplies(Comment comment, UUID currentUserId) {
    List<Comment> replies = commentRepository
        .findAllByParentComment_CommentIdAndDeletedFalseOrderByCreatedAtAsc(comment.getCommentId());

    List<CommentDto> replyDtos = replies.stream()
        .map(c -> toDtoWithReplies(c, currentUserId))
        .toList();

    long reactions = reactionRepository.countByTargetTypeAndTargetId("comment", comment.getCommentId());
    boolean userReacted = currentUserId != null
        && reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(currentUserId, "comment", comment.getCommentId());

    return new CommentDto(
        comment.getCommentId(),
        comment.getUser().getUserId(),
        comment.getUser().getFirstName(),
        comment.getContent(),
        comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null,
        comment.getCreatedAt(),
        comment.getUpdatedAt(),
        reactions,
        userReacted,
        replyDtos
    );
  }

  private CommentDto toDto(Comment comment, UUID currentUserId) {
    long reactions = reactionRepository.countByTargetTypeAndTargetId("comment", comment.getCommentId());
    boolean userReacted = currentUserId != null
        && reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(currentUserId, "comment", comment.getCommentId());
    return new CommentDto(
        comment.getCommentId(),
        comment.getUser().getUserId(),
        comment.getUser().getFirstName(),
        comment.getContent(),
        comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null,
        comment.getCreatedAt(),
        comment.getUpdatedAt(),
        reactions,
        userReacted,
        List.of()
    );
  }
}

