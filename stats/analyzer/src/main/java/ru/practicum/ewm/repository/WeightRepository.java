package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Weight;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WeightRepository extends JpaRepository<Weight, Long> {
    Optional<Weight> findByEventIdAndUserId(long eventId, long userId);

    List<Weight> findAllByEventIdIn(List<Long> eventIds);

    @Query("""
            SELECT w.eventId
            FROM Weight w
            WHERE w.userId = :userId
            """)
    Set<Long> findAllEventIdByUserId(@Param("userId")long userId);

    @Query("""
            SELECT w.eventId FROM Weight w
            WHERE w.userId = :userId
            ORDER BY w.timestamp DESC
            LIMIT :maxResults
            """)
    List<Long> findLastInteractedEventIds(@Param("userId") long userId, @Param("maxResults") int maxResults);

    @Query("""
            SELECT w
            FROM Weight w
            WHERE w.userId = :userId AND w.eventId IN :eventIds
            """)
    List<Weight> findByUserIdAndEventIdIn(@Param("userId") long userId, @Param("eventIds") Set<Long> eventIds);
}
