package com.peti.backend.repository;

import com.peti.backend.model.domain.User;
import com.peti.backend.model.projection.UserProjection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  @Query(value = """
      SELECT new com.peti.backend.model.projection.UserProjection(user.userId, user.email, user.password, user.role.roleId)
      FROM User user WHERE user.email = :email
      """)
  Optional<UserProjection> findUserDetailsByEmail(String email);

}
