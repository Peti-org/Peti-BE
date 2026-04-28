package com.peti.backend.repository;

import com.peti.backend.model.domain.Article;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

  Page<Article> findAllByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

  Page<Article> findAllByDeletedFalseAndAuthor_UserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}

