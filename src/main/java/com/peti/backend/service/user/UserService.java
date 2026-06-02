package com.peti.backend.service.user;

import static com.peti.backend.service.user.RoleService.convertToAuthority;

import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.ChangeEmailResponse;
import com.peti.backend.dto.user.RequestChangeEmail;
import com.peti.backend.dto.user.RequestUpdatePassword;
import com.peti.backend.dto.user.RequestUpdateUser;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.Role;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.UserRepository;
import com.peti.backend.security.JwtService;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final RoleService roleService;
  private final EntityManager entityManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final EmailChangeNotifier emailChangeNotifier;
  private final UserValidator userValidator;

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

  @Transactional
  public Optional<UserDto> getUserById(UUID userId) {
    return userRepository.findById(userId)
        .map(user -> {
          UserDto userDto = convertToDto(user, CityService.convertToDto(user.getCityByCityId()));
          userDto.setCaretaker(
              Optional.ofNullable(user.getCaretakersByUserId())
                  .map(caretakers -> !caretakers.isEmpty())
                  .orElse(false)
          );
          return userDto;
        });
  }

  public UserDto createUser(User user) {
    User savedUser = userRepository.save(user);
    return convertToDto(savedUser, CityService.convertToDto(savedUser.getCityByCityId()));
  }

  public UserDto updateUser(UUID userId, RequestUpdateUser updatedUser) {
    User existingUser = userValidator.findUserOrThrow(userId);
    existingUser.setFirstName(updatedUser.getFirstName());
    existingUser.setLastName(updatedUser.getLastName());
    existingUser.setBirthday(Date.valueOf(updatedUser.getBirthDate()));
    existingUser.setCityByCityId(entityManager.getReference(City.class, updatedUser.getCityId()));

    User savedUser = userRepository.save(existingUser);
    return convertToDto(savedUser, CityService.convertToDto(savedUser.getCityByCityId()));
  }

  public boolean updatePassword(UUID userId, RequestUpdatePassword passwordDto) {
    User user = userValidator.findUserOrThrow(userId);
    userValidator.verifyPassword(user, passwordDto.getOldPassword());
    user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
    userRepository.save(user);
    return true;
  }

  @Transactional
  public ChangeEmailResponse changeEmail(UUID userId, RequestChangeEmail request) {
    User user = userValidator.findUserOrThrow(userId);
    userValidator.verifyPassword(user, request.currentPassword());
    userValidator.verifyEmailNotSame(user, request.newEmail());
    userValidator.verifyEmailNotTaken(request.newEmail());

    String oldEmail = user.getEmail();
    user.setEmail(request.newEmail());
    User saved = userRepository.save(user);

    emailChangeNotifier.notifyEmailChanged(oldEmail, request.newEmail());

    AuthResponse auth = jwtService.generateAuthResponse(saved.getEmail());
    UserDto userDto = convertToDto(saved, CityService.convertToDto(saved.getCityByCityId()));
    return new ChangeEmailResponse(userDto, auth);
  }

  public void deleteUser(UUID userId) {
    userRepository.deleteById(userId);
  }

  public void changeRole(UUID userId, Role role) {
    userRepository.findById(userId)
        .map(existingUser -> {
          existingUser.setRole(role);

          User savedUser = userRepository.save(existingUser);
          return convertToDto(savedUser, CityService.convertToDto(savedUser.getCityByCityId()));
        });
  }
}
