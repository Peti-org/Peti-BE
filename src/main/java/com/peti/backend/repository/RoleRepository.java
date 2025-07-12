package com.peti.backend.repository;

import com.peti.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

  @Query(value = """
          SELECT r.role_id, r.role_name
          FROM peti.role r
          WHERE r.role_id >= (
              SELECT  role.role_id
              FROM peti.role role
              WHERE role.role_name = :name
              LIMIT 1
          )
          """, nativeQuery = true)
  List<Role> findRolesGreaterThanSelected(@Param("name") String roleName);

  Optional<Role> findTopByOrderByRoleIdDesc();
}