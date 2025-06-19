package com.peti.backend.dto;

import com.peti.backend.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
public class RegisterUserDto {

  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private Integer cityId;

  public User toUser(PasswordEncoder passwordEncoder){
    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setBirthday(Date.valueOf(birthDate));
    user.setUserDataFolder("default"); //TODO: mock folder as we don't have such a feature right now
    user.setUserIsDeleted(false);
    return user;
  }
}
