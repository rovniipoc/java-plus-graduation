package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Similarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    Optional<Similarity> findByEventAIdAndEventBId(long eventAId, long eventBId);

    @Query("""
            SELECT s
            FROM Similarity s
            WHERE s.eventAId = :eventId
               OR s.eventBId = :eventId
            ORDER BY s.score DESC
            """)
    List<Similarity> findAllByEventId(@Param("eventId") long eventId);

    @Query("""
    SELECT s
    FROM Similarity s
    WHERE (s.eventAId IN :interactedEventIds AND s.eventBId IN :candidateEventIds)
       OR (s.eventBId IN :interactedEventIds AND s.eventAId IN :candidateEventIds)
    ORDER BY s.score DESC
    """)
    List<Similarity> findSimilaritiesForRecommendation(
            @Param("interactedEventIds") Set<Long> interactedEventIds,
            @Param("candidateEventIds") Set<Long> candidateEventIds
    );
}
