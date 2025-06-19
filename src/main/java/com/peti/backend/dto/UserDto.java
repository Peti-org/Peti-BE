package com.peti.backend.dto;

import com.peti.backend.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UserDto {

  private UUID userId;
  private String email;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private UUID caretakersByUserId;
  //add pets collections
  //add location
  private CityDto city;

  /**
   * Converts a User entity to a UserDto
   *
   * @param user the User entity to convert
   * @return a new UserDto with data from the User entity
   */
  public static UserDto fromUser(User user, CityDto city) {
    if (user == null) {
      return null;
    }

    UserDto userDto = new UserDto();
    userDto.setUserId(user.getUserId());
    userDto.setEmail(user.getEmail());
    userDto.setFirstName(user.getFirstName());
    userDto.setLastName(user.getLastName());
    userDto.setBirthDate(user.getBirthday().toLocalDate());

    // Set city information
    userDto.setCity(city);

    // If there's at least one caretaker, set the first one's ID
//    if (user.getCaretakersByUserId() != null && !user.getCaretakersByUserId().isEmpty()) {
//      user.getCaretakersByUserId().stream().findFirst().ifPresent(caretaker ->
//              userDto.setCaretakersByUserId(caretaker.getCaretakerId())
//      );
//    }

    return userDto;
  }
}
