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
        WHERE s.eventAId IN :eventIds
           OR s.eventBId IN :eventIds
        ORDER BY s.score DESC
    """)
    List<Similarity> findAllBetweenCandidatesAndInteracted(@Param("eventIds") List<Long> eventIds);

    @Query("""
            SELECT s
            FROM Similarity s
            WHERE
                (s.eventAId IN :newEventIds AND s.eventBId IN :interactedEventIds)
                OR
                (s.eventBId IN :newEventIds AND s.eventAId IN :interactedEventIds)
            """)
    List<Similarity> findAllBetweenCandidatesAndInteracted(
            @Param("newEventIds") Set<Long> newEventIds,
            @Param("interactedEventIds") Set<Long> interactedEventIds);

}
