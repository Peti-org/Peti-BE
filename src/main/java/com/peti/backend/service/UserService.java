package com.peti.backend.service;

import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.User;
import com.peti.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  public List<UserDto> getAllUsers() {
    List<User> users = userRepository.findAll();
    List<UserDto> userDtos = new ArrayList<>();
    for (User user : users) {
      userDtos.add(convertToDto(user));
    }
    return userDtos;
  }

  public Optional<UserDto> getUserById(UUID id) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isPresent()) {
      return Optional.of(convertToDto(userOptional.get()));
    }
    return Optional.empty();
  }

  public UserDto createUser(User user) {
    User savedUser = userRepository.save(user);
    return convertToDto(savedUser);
  }

  public Optional<UserDto> updateUser(UUID id, User userDetails) {
    Optional<User> userOptional = userRepository.findById(id);

    if (userOptional.isPresent()) {
      User user = userOptional.get();

      user.setFirstName(userDetails.getFirstName());
      user.setLastName(userDetails.getLastName());
      user.setEmail(userDetails.getEmail());
//            user.setPhoneNumber(userDetails.getPhoneNumber());
      user.setPassword(userDetails.getPassword());

      User updatedUser = userRepository.save(user);
      return Optional.of(convertToDto(updatedUser));
    }
    return Optional.empty();
  }

  public void deleteUser(UUID id) {
    userRepository.deleteById(id);
  }

  private UserDto convertToDto(User user) {
    return UserDto.fromUser(user, new CityDto());
  }
}
