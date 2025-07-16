package com.peti.backend.service;

import static com.peti.backend.service.RoleService.convertToAuthority;

import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.user.UpdatePasswordDto;
import com.peti.backend.dto.user.UpdateUserDto;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.Role;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final RoleService roleService;
  private final EntityManager entityManager;

  private final PasswordEncoder passwordEncoder;

  public static UserDto convertToDto(User user, CityDto city) {
    if (user == null) {
      return null;
    }

    UserDto userDto = new UserDto();
    userDto.setUserId(user.getUserId());
    userDto.setEmail(user.getEmail());
    userDto.setFirstName(user.getFirstName());
    userDto.setLastName(user.getLastName());
    userDto.setBirthDate(user.getBirthday().toLocalDate());
    userDto.setRoleName(user.getRole().getRoleName());
    userDto.setCity(city);

    // If there's at least one caretaker, set the first one's ID
//    if (user.getCaretakersByUserId() != null && !user.getCaretakersByUserId().isEmpty()) {
//      user.getCaretakersByUserId().stream().findFirst().ifPresent(caretaker ->
//              userDto.setCaretakersByUserId(caretaker.getCaretakerId())
//      );
//    }

    return userDto;
  }


  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream()
        .map(user -> convertToDto(user, CityService.convertToDto(user.getCityByCityId())))
        .toList();
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findUserDetailsByEmail(username)
        .map(userProjection -> {
          int roleId = userProjection.getRoleId();
          Role role = roleService.getRoleById(roleId);
          userProjection.setAuthorities(Collections.singletonList(convertToAuthority(role)));
          return userProjection;
        })
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }


  public Optional<UserDto> getUserById(UUID userId) {
    return userRepository.findById(userId)
        .map(user -> convertToDto(user, CityService.convertToDto(user.getCityByCityId())));
  }

  public UserDto createUser(User user) {
    User savedUser = userRepository.save(user);
    return convertToDto(savedUser, CityService.convertToDto(savedUser.getCityByCityId()));
  }

  public UserDto updateUser(UUID userId, UpdateUserDto updatedUser) {
    return userRepository.findById(userId)
        .map(existingUser -> {
          existingUser.setFirstName(updatedUser.getFirstName());
          existingUser.setLastName(updatedUser.getLastName());
          existingUser.setBirthday(Date.valueOf(updatedUser.getBirthDate()));
          existingUser.setCityByCityId(entityManager.getReference(City.class, updatedUser.getCityId()));

          User savedUser = userRepository.save(existingUser);
          return convertToDto(savedUser, CityService.convertToDto(savedUser.getCityByCityId()));
        })
        .orElse(null);
  }

  public boolean updatePassword(UUID userId, UpdatePasswordDto passwordDto) {
    return userRepository.findById(userId)
        .filter(user -> passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword()))
        .map(existingUser -> {
          existingUser.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
          userRepository.save(existingUser);
          return true;
        })
        .orElse(false);
  }

  public void deleteUser(UUID userId) {
    userRepository.deleteById(userId);
  }
}
