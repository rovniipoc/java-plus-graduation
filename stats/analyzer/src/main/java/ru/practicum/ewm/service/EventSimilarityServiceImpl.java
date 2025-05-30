package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.mapper.RecommendationsMapper;
import ru.practicum.ewm.mapper.SimilarityMapper;
import ru.practicum.ewm.model.RecommendedEvent;
import ru.practicum.ewm.model.Similarity;
import ru.practicum.ewm.model.Weight;
import ru.practicum.ewm.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.message.RecommendedEventProto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityServiceImpl implements EventSimilarityService {
    private static final int NEIGHBOR_QUANTITY = 5;

    private final SimilarityMapper similarityMapper;
    private final RecommendationsMapper recommendationsMapper;
    private final SimilarityRepository similarityRepository;
    private final UserActionService userActionService;

    @Transactional
    public void updateOrCreateSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        Similarity received = similarityMapper.mapToSimilarity(eventSimilarityAvro);
        similarityRepository.findByEventAIdAndEventBId(received.getEventAId(), received.getEventBId()
        ).ifPresentOrElse(
                old -> updateSimilarity(old, received),
                () -> saveNewSimilarity(received)
        );
    }

    private void updateSimilarity(Similarity old, Similarity received) {
        log.info("Updating similarity: event-A id: {}, event-B id: {}, old score: {}, new score: {}",
                old.getEventAId(), old.getEventBId(), old.getScore(), received.getScore());
        old.setScore(received.getScore());
        old.setTimestamp(received.getTimestamp());
        similarityRepository.save(old);
    }

    private void saveNewSimilarity(Similarity received) {
        log.info("Saving new similarity: eventAId: {}, eventBId: {}, score: {}",
                received.getEventAId(), received.getEventBId(), received.getScore());
        similarityRepository.save(received);
    }

    public List<RecommendedEventProto> getSimilarEvents(long userId, long sampleEventId, int limit) {
        List<Similarity> similarities = similarityRepository.findAllByEventId(sampleEventId);
        log.debug("Fetched {} similarities for eventId: {}", similarities.size(), sampleEventId);

        Set<Long> interactedEventIds = userActionService.getAllEventIdByUserId(userId);

        List<RecommendedEvent> recommendedEvents = similarities.stream()
                .map(similarity -> {
                    // Определяем похожее событие
                    long otherEventId = similarity.getEventAId() == sampleEventId
                            ? similarity.getEventBId()
                            : similarity.getEventAId();
                    return new RecommendedEvent(otherEventId, similarity.getScore());
                })
                .filter(re -> !interactedEventIds.contains(re.eventId()))
                .limit(limit)
                .toList();
        log.debug("Find recommended events for user: {}, similar to event: {} - {}",
                userId, sampleEventId, recommendedEvents);

        return recommendationsMapper.mapToProto(recommendedEvents);
    }


    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int limit) {
        // 1. Выгрузить мероприятия, с которыми пользователь недавно взаимодействовал
        List<Long> lastInteracted = userActionService.getLastInteractedEvents(userId, limit);
        if (lastInteracted.isEmpty()) {
            log.debug("No interacted events found for user {}", userId);
            return Collections.emptyList();
        }

        // 2. Найти похожие новые
        List<Similarity> similarToInteracted = similarityRepository.findAllBetweenCandidatesAndInteracted(lastInteracted);
        log.debug("Fetched {} similarities ", similarToInteracted.size());

        // 3. Отобрать те с которыми пользователь не взаимодействовал
        Set<Long> interacted = userActionService.getAllEventIdByUserId(userId);
        List<Long> recommendedCandidates = getNotInteractedEvents(similarToInteracted, interacted, limit);

        // 4. Найти K ближайших (по similarity) соседей для кандидатов в рекомендацию среди просмотренных мероприятий
        List<Similarity> interactedSimilarToCandidates = similarityRepository
                .findAllBetweenCandidatesAndInteracted(new HashSet<>(recommendedCandidates), interacted);
        Set<Long> neighborEventIds = interactedSimilarToCandidates.stream()
                .map(s ->
                        recommendedCandidates.contains(s.getEventAId()) ?
                                s.getEventBId() :
                                s.getEventAId())
                .collect(Collectors.toSet());

        // 5. Для всех соседей, полученных на предыдущем этапе, выгрузить оценки, которые поставил пользователь
        List<Weight> neighborWeights = userActionService.getByUserIdAndEventIds(userId, neighborEventIds);
        Map<Long, Double> weightsMap = neighborWeights.stream()
                .collect(Collectors.toMap(Weight::getEventId, Weight::getWeight));

        // 6. Map candidateId → List<Neighbor>
        Map<Long, List<Neighbor>> candidateNeighborsMap = buildCandidateNeighborsMap(
                interactedSimilarToCandidates,
                new HashSet<>(recommendedCandidates),
                weightsMap
        );

        List<RecommendedEvent> recommendedEvents = new ArrayList<>();
        for (Map.Entry<Long, List<Neighbor>> entry : candidateNeighborsMap.entrySet()) {
            long candidateId = entry.getKey();

            List<Neighbor> neighbors = entry.getValue().stream()
                    .sorted(Comparator.comparingDouble(Neighbor::similarity).reversed())
                    .limit(NEIGHBOR_QUANTITY)
                    .toList();

            // 9. Вычислить оценку нового мероприятия. Поделить сумму взвешенных оценок на сумму коэффициентов.
            double score = calculateScore(neighbors);
            log.debug("Predicted score for candidate {} = {}", candidateId, score);
            recommendedEvents.add(new RecommendedEvent(candidateId, score));
        }
        recommendedEvents = recommendedEvents.stream()
                .sorted(Comparator.comparingDouble(RecommendedEvent::score).reversed())
                .limit(limit)
                .toList();

        log.debug("Recommended events for user {}: {}", userId, recommendedEvents);
        return recommendationsMapper.mapToProto(recommendedEvents);
    }



    private List<Long> getNotInteractedEvents(List<Similarity> similarities, Set<Long> interacted, int limit) {
        return similarities.stream()
                .map(s -> {
                    Long a = s.getEventAId();
                    Long b = s.getEventBId();
                    if (interacted.contains(a) && !interacted.contains(b)) {
                        return b; // Если взаимодействовали с A, но не с B — кандидат для рекомендации B
                    }
                    if (interacted.contains(b) && !interacted.contains(a)) {
                        return a; // Если взаимодействовали с B, но не с A — кандидат для рекомендации A
                    }
                    return null; // Если взаимодействовали с A и B — не подходит
                })
                .filter(Objects::nonNull)
                .distinct()
                .limit(limit)
                .toList();
    }

    private Map<Long, List<Neighbor>> buildCandidateNeighborsMap(
            List<Similarity> similarities,
            Set<Long> recommendedCandidateIds,
            Map<Long, Double> weightsMap
    ) {
        Map<Long, List<Neighbor>> candidateNeighborsMap = new HashMap<>();

        for (Similarity s : similarities) {
            long candidateId = recommendedCandidateIds.contains(s.getEventAId()) ? s.getEventAId() : s.getEventBId();
            long neighborId = candidateId == s.getEventAId() ? s.getEventBId() : s.getEventAId();
            double similarity = s.getScore();
            Double weight = weightsMap.get(neighborId);

            if (weight != null) {
                candidateNeighborsMap
                        .computeIfAbsent(candidateId, id -> new ArrayList<>())
                        .add(new Neighbor(neighborId, similarity, weight));
            }
        }
        return candidateNeighborsMap;
    }

    private double calculateScore(List<Neighbor> neighbors) {
        double numerator = 0.0;
        double denominator = 0.0;
        for (Neighbor neighbor : neighbors) {
            // 7. Вычислить сумму взвешенных оценок (перемножить оценки мероприятий с их коэффициентами подобия, все полученные произведения сложить)
            numerator += neighbor.weight() * neighbor.similarity();
            // 8. Вычислить сумму коэффициентов подобия. Сложить все коэффициенты подобия, полученные на шаге 4
            denominator += neighbor.similarity();
        }
        return denominator == 0.0 ? 0.0 : numerator / denominator;
    }

    private record Neighbor(
            long eventId,
            double similarity,
            double weight) {
    }
}
