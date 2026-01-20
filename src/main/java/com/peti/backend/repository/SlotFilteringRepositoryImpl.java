package com.peti.backend.repository;

import com.peti.backend.dto.slot.SlotCursor;
import com.peti.backend.dto.slot.SlotFiltersDto;
import com.peti.backend.model.domain.Slot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SlotFilteringRepositoryImpl implements SlotFilteringRepository {

  @PersistenceContext
  private final EntityManager entityManager;

  @Override
  public List<Slot> findSlotsWithCursor(@NonNull SlotFiltersDto filter, @NonNull SlotCursor cursor) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT s FROM Slot s WHERE ");

    // Cursor condition
    sb.append("( s.caretaker.rating < :cursorRating ")
        .append(" OR (s.caretaker.rating = :cursorRating AND s.creationTime < :cursorCreatedAt) ")
        .append(")")
        .append(" AND s.available IS TRUE");

    // Filters
    filter.onDate().ifPresent(t -> sb.append(" AND s.date = :onDate "));
    filter.minPrice().ifPresent(p -> sb.append(" AND s.price >= :minPrice "));
    filter.maxPrice().ifPresent(p -> sb.append(" AND s.price <= :maxPrice "));
    filter.type().ifPresent(t -> sb.append(" AND s.type = :type "));
    filter.timeFrom().ifPresent(t -> sb.append(" AND s.timeFrom >= :timeFrom "));
    filter.timeTo().ifPresent(t -> sb.append(" AND s.timeTo <= :timeTo "));

    sb.append(" ORDER BY s.caretaker.rating DESC, s.creationTime DESC");

    TypedQuery<Slot> query = entityManager.createQuery(sb.toString(), Slot.class);
    // Cursor values
    query.setParameter("cursorRating", cursor.userRating());
    query.setParameter("cursorCreatedAt", cursor.createdAt());

    // Set filters
    filter.onDate().ifPresent(t -> query.setParameter("onDate", t));
    filter.minPrice().ifPresent(p -> query.setParameter("minPrice", p));
    filter.maxPrice().ifPresent(p -> query.setParameter("maxPrice", p));
    filter.type().ifPresent(t -> query.setParameter("type", t));
    filter.timeFrom().ifPresent(t -> query.setParameter("timeFrom", t));
    filter.timeTo().ifPresent(t -> query.setParameter("timeTo", t));

    query.setMaxResults(cursor.limit());
    return query.getResultList();
  }
}
