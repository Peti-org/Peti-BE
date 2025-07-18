package com.peti.backend.service;

import com.peti.backend.model.Role;
import com.peti.backend.repository.RoleRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RoleService implements RoleHierarchy {

  private static final String ROLE_PREFIX = "ROLE_";

  private final RoleRepository roleRepository;

  @Getter
  private Role userRole;
  @Getter
  private Role careTakerRole;
  @Getter
  private Role adminRole;

  private Map<String, List<Role>> roleHierarchyMap;

  public static SimpleGrantedAuthority convertToAuthority(Role role) {
    return new SimpleGrantedAuthority(ROLE_PREFIX + role.getRoleName());
  }

  @EventListener(ApplicationReadyEvent.class)
  public void updateRoles() {
    userRole = roleRepository.findByRoleName("USER").orElseThrow();
    careTakerRole = roleRepository.findByRoleName("CARETAKER").orElseThrow();
    adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();
    roleHierarchyMap = roleRepository.findAll().stream()
        .collect(Collectors.toMap(
            Role::getRoleName,
            role -> roleRepository.findRolesGreaterThanSelected(role.getRoleName())
        ));
  }

  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }

  public Role getRoleById(Integer id) {
    return roleRepository.findById(id).orElse(null);
  }

  public Role getLowestRole() {
    return roleRepository.findTopByOrderByRoleIdDesc().orElse(null);
  }

  @Override
  public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
      Collection<? extends GrantedAuthority> authorities) {

    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> auth.startsWith(ROLE_PREFIX))
        .map(auth -> auth.substring(ROLE_PREFIX.length()))
        .flatMap(auth -> roleHierarchyMap.getOrDefault(auth, List.of()).stream())
        .map(RoleService::convertToAuthority)
        .collect(Collectors.toList());
  }
}
