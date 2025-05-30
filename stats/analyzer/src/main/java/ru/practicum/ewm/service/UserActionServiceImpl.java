package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.mapper.WeightMapper;
import ru.practicum.ewm.model.Weight;
import ru.practicum.ewm.repository.WeightRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.message.RecommendedEventProto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionServiceImpl implements UserActionService {
    private final WeightRepository weightRepository;
    private final WeightMapper weightMapper;

    public void updateOrCreateWeight(UserActionAvro userActionAvro) {
        Weight newWeight = weightMapper.toWeight(userActionAvro);
        weightRepository.findByEventIdAndUserId(newWeight.getEventId(), newWeight.getUserId())
                .ifPresentOrElse(
                        existingWeight -> updateIfGreater(existingWeight, newWeight),
                        () -> saveNewWeight(newWeight)
                );
    }

    public List<RecommendedEventProto> getTotalInteractionWeight(List<Long> eventIds) {
        List<Weight> weights = weightRepository.findAllByEventIdIn(eventIds);
        log.debug("Weights: {} for list eventIds: {}", weights, eventIds);
        Map<Long, Double> totalWeights = weights.stream()
                .collect(Collectors.groupingBy(
                        Weight::getEventId,
                        Collectors.summingDouble(Weight::getWeight)
                ));
        log.debug("Count of total interaction weights: {}", totalWeights);
        return totalWeights.entrySet().stream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Weight> getByUserIdAndEventIds(long userId, Set<Long> eventIds) {
        List<Weight> weights = weightRepository.findByUserIdAndEventIdIn(userId, eventIds);
        log.debug("Fetched {} weights for user: {} interacted events: {}", weights.size(), userId, weights);
        return weights;
    }

    public Set<Long> getAllEventIdByUserId(long userId) {
        Set<Long> eventIds = weightRepository.findAllEventIdByUserId(userId);
        log.debug("Fetched {} events for user {}", eventIds.size(), userId);
        return eventIds;
    }

    public List<Long> getLastInteractedEvents(long userId, int limit) {
        List<Long> lastInteracted = weightRepository.findLastInteractedEventIds(userId, limit);
        log.debug("Fetched {} last interacted by user: {} events: {}",
                lastInteracted.size(), userId, lastInteracted);
        return lastInteracted;
    }

    private void updateIfGreater(Weight existingWeight, Weight newWeight) {
        if (newWeight.getWeight() > existingWeight.getWeight()) {
            log.info("Updating weight for userId: {}, eventId: {} from {} to {}",
                    existingWeight.getUserId(),
                    existingWeight.getEventId(),
                    existingWeight.getWeight(),
                    newWeight.getWeight());
            existingWeight.setWeight(newWeight.getWeight());
            existingWeight.setTimestamp(newWeight.getTimestamp());
            weightRepository.save(existingWeight);
        } else {
            log.info("Existing weight: {} for userId: {}, eventId: {} is greater or equal to new weight: {}, skipping update",
                    existingWeight.getWeight(),
                    existingWeight.getUserId(),
                    existingWeight.getEventId(),
                    newWeight.getWeight());
        }
    }

    private void saveNewWeight(Weight newWeight) {
        log.info("Saving new weight for userId={}, eventId={}, weight={}",
                newWeight.getUserId(),
                newWeight.getEventId(),
                newWeight.getWeight());
        Weight weight = weightRepository.save(newWeight);
        log.debug("Weight saved: {}", weight);
    }
}
