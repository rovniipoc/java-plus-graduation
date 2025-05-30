package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.KafkaSender;
import ru.practicum.ewm.repository.InteractionMatrixRepository;
import ru.practicum.ewm.repository.MinWeightSumRepository;
import ru.practicum.ewm.repository.TotalWeightRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionProcessorImpl implements UserActionProcessor {
    private final InteractionMatrixRepository interactionMatrixRepository;
    private final TotalWeightRepository totalWeightRepository;
    private final MinWeightSumRepository minWeightSumRepository;
    private final KafkaSender kafkaSender;

    public void process(UserActionAvro userAction) {
        long userId = userAction.getUserId();
        long eventId = userAction.getEventId();
        double oldWeight = getCurrentWeight(eventId, userId);
        double newWeight = getReceivedWeight(userAction);
        if (newWeight <= oldWeight) {
            log.debug("New weight: {} is not greater than old weight: {}. Skipping update.", newWeight, oldWeight);
            return;
        }
        updateInteractionWeight(eventId, userId, newWeight);
        double newTotalEventWeight = updateTotalEventWeight(eventId, oldWeight, newWeight);
        Set<Long> otherEventsInteractedByUser = getOtherEventsInteractedByUser(userId, eventId);
        for (Long otherEventId : otherEventsInteractedByUser) {
            double minWeightSum = updateMinWeightSum(eventId, otherEventId, userId, oldWeight, newWeight);
            double otherEventWeight = totalWeightRepository.get(otherEventId);
            float similarity = (float) calculateEventSimilarity(minWeightSum, newTotalEventWeight, otherEventWeight);
            kafkaSender.sendSimilarity(eventId, otherEventId, similarity);
        }
    }

    private double getReceivedWeight(UserActionAvro userAction) {
        double weight = switch (userAction.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
        log.debug("Received interaction weight: {}. userId: {}, eventId: {}, actionType: {}",
                weight, userAction.getUserId(), userAction.getEventId(), userAction.getActionType());
        return weight;
    }

    private double getCurrentWeight(long eventId, long userId) {
        return interactionMatrixRepository.getWeight(eventId, userId);
    }

    private void updateInteractionWeight(long eventId, long userId, double newWeight) {
        interactionMatrixRepository.put(eventId, userId, newWeight);
    }

    private Set<Long> getOtherEventsInteractedByUser(long userId, long eventId) {
        return interactionMatrixRepository.getEvents(userId).stream()
                .filter(otherEventId -> otherEventId != eventId)
                .collect(Collectors.toSet());
    }

    private double updateTotalEventWeight(long eventId, double currentWeight, double newWeight) {
        double oldTotalWeight = totalWeightRepository.get(eventId);
        double delta = newWeight - currentWeight;
        double newTotalWeight = oldTotalWeight + delta;
        totalWeightRepository.put(eventId, newTotalWeight);
        return newTotalWeight;
    }

    private double updateMinWeightSum(long eventId, long otherEventId, long userId, double oldWeight, double newWeight) {
        double otherEventWeight = interactionMatrixRepository.getWeight(otherEventId, userId);
        double oldMin = Math.min(oldWeight, otherEventWeight); //старый вклад пользователя в общую сумму
        double minNew = Math.min(newWeight, otherEventWeight); //новый вклад пользователя в общую сумму
        double delta = minNew - oldMin;
        double oldMinSum = minWeightSumRepository.get(eventId, otherEventId);
        if (delta != 0) {
            double newMinSum = oldMinSum + delta;
            minWeightSumRepository.put(eventId, otherEventId, newMinSum);
            log.debug("Updated min weight sum for event pair ({}, {}): delta = {}", eventId, otherEventId, delta);
            return newMinSum;
        } else {
            log.debug("No change in min weight for event pair ({}, {})", eventId, otherEventId);
            return oldMinSum;
        }
    }

    private double calculateEventSimilarity(
            double minWeightSum,
            double totalEventWeight,
            double totalOtherEventWeight) {
        double denominator = Math.sqrt(totalEventWeight) * Math.sqrt(totalOtherEventWeight);
        return minWeightSum / denominator;
    }
}

