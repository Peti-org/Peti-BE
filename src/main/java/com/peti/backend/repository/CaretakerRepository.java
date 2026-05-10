package com.peti.backend.repository;

import com.peti.backend.model.domain.Caretaker;
import java.util.Optional;
import java.util.UUID;
import com.peti.backend.model.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CaretakerRepository extends JpaRepository<Caretaker, UUID> {

  @Query(value = """
      SELECT caretacker.caretakerId
      FROM Caretaker caretacker WHERE caretacker.userReference.userId = :userId
      """)
  Optional<UUID> findCaretakerIdBy(UUID userId);

  Boolean existsByUserReference_UserId(UUID userReferenceUserId);

  Optional<Caretaker> findByUserReference_UserId(UUID userReferenceUserId);

  List<Caretaker> findAllByUserReference_CityByCityId_CityId(Long cityId);
}
