package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static com.peti.backend.service.RoleService.convertToAuthority;

@Entity
@Getter
@Setter
@Table(name = "user", schema = "peti", catalog = "peti")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class User implements UserDetails {
  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @Column(name = "user_id", nullable = false)
  @EqualsAndHashCode.Include
  private UUID userId;
  @Basic
  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;
  @Basic
  @Column(name = "last_name", length = 50)
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
  @OneToMany(mappedBy = "petOwner")
  private Collection<Pet> petsByUserId;
  @ManyToOne
  @JoinColumn(name = "location_id", referencedColumnName = "location_id", nullable = true)
  private Location locationByLocationId;

  @ManyToOne
  @JoinColumn(name = "city_id", referencedColumnName = "city_id", nullable = false)
  private City cityByCityId;

  @ManyToOne
  @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
  private Role role;

  public User(UUID userId) {
    this.userId = userId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(convertToAuthority(role));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
