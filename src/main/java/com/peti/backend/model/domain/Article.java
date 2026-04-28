package com.peti.backend.model.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "article", schema = "peti", catalog = "peti")
public class Article {

  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @EqualsAndHashCode.Include
  @Column(name = "article_id", nullable = false)
  private UUID articleId;

  @Basic
  @Column(name = "title", nullable = false, length = 300)
  private String title;

  @Basic
  @Column(name = "summary", length = 1000)
  private String summary;

  @Basic
  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "tags", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private List<String> tags;

  @Basic
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Basic
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Basic
  @Column(name = "estimated_read_minutes", nullable = false)
  private int estimatedReadMinutes;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User author;

  @Basic
  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;
}

