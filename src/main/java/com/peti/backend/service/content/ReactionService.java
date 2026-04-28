package com.peti.backend.service.content;

import com.peti.backend.dto.content.RequestReactionDto;
import com.peti.backend.model.domain.Reaction;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.ReactionRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionService {

  private final ReactionRepository reactionRepository;
  private final EntityManager entityManager;

  /**
   * Toggles a reaction: creates if not present, deletes if already exists.
   *
   * @return true if reaction was added, false if removed
   */
  @Transactional
  public boolean toggleReaction(RequestReactionDto request, UUID userId) {
    return reactionRepository
        .findByUser_UserIdAndTargetTypeAndTargetId(userId, request.targetType(), request.targetId())
        .map(existing -> {
          reactionRepository.delete(existing);
          return false;
        })
        .orElseGet(() -> {
          Reaction reaction = new Reaction();
          reaction.setUser(entityManager.getReference(User.class, userId));
          reaction.setTargetType(request.targetType());
          reaction.setTargetId(request.targetId());
          reaction.setCreatedAt(LocalDateTime.now());
          reactionRepository.save(reaction);
          return true;
        });
  }

  public boolean hasUserReacted(UUID userId, String targetType, UUID targetId) {
    return reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
  }

  public long countReactions(String targetType, UUID targetId) {
    return reactionRepository.countByTargetTypeAndTargetId(targetType, targetId);
  }
}

