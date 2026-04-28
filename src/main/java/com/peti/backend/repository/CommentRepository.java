package com.peti.backend.repository;

import com.peti.backend.model.domain.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

  List<Comment> findAllByTargetTypeAndTargetIdAndParentCommentIsNullAndDeletedFalseOrderByCreatedAtAsc(
      String targetType, UUID targetId);

  List<Comment> findAllByParentComment_CommentIdAndDeletedFalseOrderByCreatedAtAsc(UUID parentCommentId);

  long countByTargetTypeAndTargetIdAndDeletedFalse(String targetType, UUID targetId);
}

