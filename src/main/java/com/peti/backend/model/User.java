package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.Collection;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "user", schema = "peti", catalog = "peti")
public class User {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "user_id", nullable = false)
  private UUID userId;
  @Basic
  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;
  @Basic
  @Column(name = "last_name", nullable = true, length = 50)
  private String lastName;
  @Basic
  @Column(name = "email", nullable = false, length = 50)
  private String email;
  @Basic
  @Column(name = "birthday", nullable = false)
  private Date birthday;
  @Basic
  @Column(name = "password", nullable = false, length = 32)
  private String password;
  @Basic
  @Column(name = "user_is_deleted", nullable = false)
  private boolean userIsDeleted;
  @Basic
  @Column(name = "user_data_folder", nullable = false, length = 50)
  private String userDataFolder;
  @OneToMany(mappedBy = "userByUserId")
  private Collection<Caretaker> caretakersByUserId;
//  @OneToMany(mappedBy = "userByUserId")
//  private Collection<PaymentSettings> paymentSettingsByUserId;
  @OneToMany(mappedBy = "userByUserId")
  private Collection<Pet> petsByUserId;
  @ManyToOne
  @JoinColumn(name = "location_id", referencedColumnName = "location_id", nullable = false)
  private Location locationByLocationId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    User user = (User) o;

    if (userIsDeleted != user.userIsDeleted)
      return false;
    if (userId != null ? !userId.equals(user.userId) : user.userId != null)
      return false;
    if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null)
      return false;
    if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null)
      return false;
    if (email != null ? !email.equals(user.email) : user.email != null)
      return false;
    if (birthday != null ? !birthday.equals(user.birthday) : user.birthday != null)
      return false;
    if (password != null ? !password.equals(user.password) : user.password != null)
      return false;
    if (userDataFolder != null ? !userDataFolder.equals(user.userDataFolder) : user.userDataFolder != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = userId != null ? userId.hashCode() : 0;
    result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
    result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
    result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
    result = 31 * result + (password != null ? password.hashCode() : 0);
    result = 31 * result + (userIsDeleted ? 1 : 0);
    result = 31 * result + (userDataFolder != null ? userDataFolder.hashCode() : 0);
    return result;
  }

}
