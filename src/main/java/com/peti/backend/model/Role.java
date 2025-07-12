package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "role", schema = "peti", catalog = "peti")
public class Role {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "role_id", nullable = false)
  @EqualsAndHashCode.Include
  private int roleId;
  @Basic
  @Column(name = "role_name", nullable = false, length = 50)
  private String roleName;

}
