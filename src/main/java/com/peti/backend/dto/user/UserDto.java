package com.peti.backend.dto.user;

import com.peti.backend.dto.CityDto;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

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
  private String roleName;
}
