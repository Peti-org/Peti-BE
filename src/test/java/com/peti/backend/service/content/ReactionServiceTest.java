package com.peti.backend.service.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.dto.content.RequestReactionDto;
import com.peti.backend.model.domain.Reaction;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.ReactionRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

  @Mock
  private ReactionRepository reactionRepository;
  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private ReactionService reactionService;

  @Test
  void toggleReaction_notExisting_createsAndReturnsTrue() {
    UUID userId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    RequestReactionDto request = new RequestReactionDto("article", targetId);

    when(reactionRepository.findByUser_UserIdAndTargetTypeAndTargetId(userId, "article", targetId))
        .thenReturn(Optional.empty());
    when(entityManager.getReference(User.class, userId)).thenReturn(new User(userId));
    when(reactionRepository.save(any(Reaction.class))).thenAnswer(inv -> inv.getArgument(0));

    boolean result = reactionService.toggleReaction(request, userId);

    assertTrue(result);
    verify(reactionRepository).save(any(Reaction.class));
  }

  @Test
  void toggleReaction_existing_deletesAndReturnsFalse() {
    UUID userId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    RequestReactionDto request = new RequestReactionDto("article", targetId);

    Reaction existing = new Reaction();
    existing.setReactionId(UUID.randomUUID());

    when(reactionRepository.findByUser_UserIdAndTargetTypeAndTargetId(userId, "article", targetId))
        .thenReturn(Optional.of(existing));

    boolean result = reactionService.toggleReaction(request, userId);

    assertFalse(result);
    verify(reactionRepository).delete(existing);
  }

  @Test
  void countReactions_returnsCount() {
    UUID targetId = UUID.randomUUID();
    when(reactionRepository.countByTargetTypeAndTargetId("news", targetId)).thenReturn(7L);

    long count = reactionService.countReactions("news", targetId);

    assertEquals(7, count);
  }

  @Test
  void hasUserReacted_returnsTrue() {
    UUID userId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    when(reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(userId, "article", targetId))
        .thenReturn(true);

    assertTrue(reactionService.hasUserReacted(userId, "article", targetId));
  }

  @Test
  void hasUserReacted_returnsFalse() {
    UUID userId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    when(reactionRepository.existsByUser_UserIdAndTargetTypeAndTargetId(userId, "article", targetId))
        .thenReturn(false);

    assertFalse(reactionService.hasUserReacted(userId, "article", targetId));
  }
}

