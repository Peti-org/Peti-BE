package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "pet", schema = "peti", catalog = "peti")
public class Pet {

  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @Column(name = "pet_id", nullable = false)
  private UUID petId;
  @Basic
  @Column(name = "name", nullable = false, length = 50)
  private String name;
  @Basic
  @Column(name = "birthday", nullable = false)
  private Date birthday;
  @Basic
  @Column(name = "context", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private Object context;
  @Basic
  @Column(name = "pet_data_folder", nullable = false, length = 50)
  private String petDataFolder;
  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User petOwner;
  @ManyToOne
  @JoinColumn(name = "breed_id", referencedColumnName = "breed_id", nullable = false)
  private Breed breed;
}
