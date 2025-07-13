package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.Role;
import com.peti.backend.repository.RoleRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

  @Mock
  private RoleRepository roleRepository;

  @InjectMocks
  private RoleService roleService;

  private Role role1;
  private Role role2;

  @BeforeEach
  public void setUp() {
    role1 = new Role();
    role1.setRoleId(1);
    role1.setRoleName("ADMIN");

    role2 = new Role();
    role2.setRoleId(2);
    role2.setRoleName("USER");
  }

  @Test
  public void testGetAllRoles() {
    List<Role> roles = Arrays.asList(role1, role2);
    when(roleRepository.findAll()).thenReturn(roles);

    List<Role> result = roleService.getAllRoles();
    assertEquals(2, result.size());
    verify(roleRepository).findAll();
  }

  @Test
  public void testGetRoleById_Found() {
    when(roleRepository.findById(1)).thenReturn(Optional.of(role1));

    Role result = roleService.getRoleById(1);
    assertNotNull(result);
    assertEquals("ADMIN", result.getRoleName());
    verify(roleRepository).findById(1);
  }

  @Test
  public void testGetRoleById_NotFound() {
    when(roleRepository.findById(100)).thenReturn(Optional.empty());

    Role result = roleService.getRoleById(100);
    assertNull(result);
    verify(roleRepository).findById(100);
  }

  @Test
  public void testGetLowestRole_Found() {
    when(roleRepository.findTopByOrderByRoleIdDesc()).thenReturn(Optional.of(role2));

    Role result = roleService.getLowestRole();
    assertNotNull(result);
    assertEquals("USER", result.getRoleName());
    verify(roleRepository).findTopByOrderByRoleIdDesc();
  }

  @Test
  public void testGetLowestRole_NotFound() {
    when(roleRepository.findTopByOrderByRoleIdDesc()).thenReturn(Optional.empty());

    Role result = roleService.getLowestRole();
    assertNull(result);
    verify(roleRepository).findTopByOrderByRoleIdDesc();
  }

  @Test
  public void testConvertToAuthority() {
    GrantedAuthority authority = RoleService.convertToAuthority(role1);
    assertEquals("ROLE_ADMIN", authority.getAuthority());
  }

  @Test
  public void testGetReachableGrantedAuthorities() {
    // Given an authority with prefix "ROLE_"
    GrantedAuthority authority = () -> "ROLE_ADMIN";
    List<GrantedAuthority> authorities = Collections.singletonList(authority);

    Role extraRole = new Role();
    extraRole.setRoleId(3);
    extraRole.setRoleName("SUPERADMIN");
    when(roleRepository.findRolesGreaterThanSelected("ADMIN"))
        .thenReturn(Collections.singletonList(extraRole));

    Collection<? extends GrantedAuthority> results = roleService.getReachableGrantedAuthorities(authorities);
    assertFalse(results.isEmpty());
    assertTrue(results.stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
    verify(roleRepository).findRolesGreaterThanSelected("ADMIN");
  }

  @Test
  public void testGetReachableGrantedAuthorities_NoMatchingPrefix() {
    // Authority that does not start with "ROLE_"
    GrantedAuthority authority = () -> "NOT_A_ROLE";
    List<GrantedAuthority> authorities = Collections.singletonList(authority);

    Collection<? extends GrantedAuthority> results = roleService.getReachableGrantedAuthorities(authorities);
    assertTrue(results.isEmpty());
    verify(roleRepository, never()).findRolesGreaterThanSelected(anyString());
  }
}
