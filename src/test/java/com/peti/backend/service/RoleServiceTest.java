package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.Role;
import com.peti.backend.repository.RoleRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

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

    Map<String, List<Role>> roleHierarchyMap = Map.of(
        "ADMIN", List.of(role1, role2),
        "USER", List.of(role2)
    );
    Map<Integer, Role> roleMapById = Map.of(
        1, role1,
        2, role2
    );

    ReflectionTestUtils.setField(roleService, "roleHierarchyMap", roleHierarchyMap);
    ReflectionTestUtils.setField(roleService, "roleMapById", roleMapById);

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
    Role result = roleService.getRoleById(1);
    assertNotNull(result);
    assertEquals("ADMIN", result.getRoleName());
    verify(roleRepository, never()).findById(1);
  }

  @Test
  public void testGetRoleById_NotFound() {
    Role result = roleService.getRoleById(100);
    assertNull(result);
    verify(roleRepository, never()).findById(100);
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
    //todo need to refactor method and test
    // Given an authority with prefix "ROLE_"
    GrantedAuthority authority = () -> "ROLE_ADMIN";
    List<GrantedAuthority> authorities = Collections.singletonList(authority);

    Role extraRole = new Role();
    extraRole.setRoleId(3);
    extraRole.setRoleName("ADMIN");
//    when(roleRepository.findRolesGreaterThanSelected("ADMIN"))
//        .thenReturn(Collections.singletonList(extraRole));

    Collection<? extends GrantedAuthority> results = roleService.getReachableGrantedAuthorities(authorities);
    assertFalse(results.isEmpty());
    assertEquals(2, results.size());
    assertTrue(results.stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    verify(roleRepository, never()).findRolesGreaterThanSelected("USER");
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

  @Test
  public void testUpdateRoles_PopulatesRolesAndHierarchy() {
    Role careTakerRole = new Role();
    careTakerRole.setRoleId(3);
    careTakerRole.setRoleName("CARETAKER");

    when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role2));
    when(roleRepository.findByRoleName("CARETAKER")).thenReturn(Optional.of(careTakerRole));
    when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(role1));
    when(roleRepository.findAll()).thenReturn(Arrays.asList(role1, role2, careTakerRole));
    when(roleRepository.findRolesGreaterThanSelected("ADMIN")).thenReturn(List.of());
    when(roleRepository.findRolesGreaterThanSelected("USER")).thenReturn(List.of());
    when(roleRepository.findRolesGreaterThanSelected("CARETAKER")).thenReturn(List.of());

    roleService.updateRoles();

    assertEquals(role2, roleService.getUserRole());
    assertEquals(careTakerRole, roleService.getCareTakerRole());
    assertEquals(role1, roleService.getAdminRole());
  }

  // Test updateRoles() throws when a role is missing
  @Test
  public void testUpdateRoles_ThrowsWhenRoleMissing() {
    when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> roleService.updateRoles());
  }
}
