package com.peti.backend.repository;

import com.peti.backend.model.domain.Reaction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

  long countByTargetTypeAndTargetId(String targetType, UUID targetId);

  Optional<Reaction> findByUser_UserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

  boolean existsByUser_UserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);
}

