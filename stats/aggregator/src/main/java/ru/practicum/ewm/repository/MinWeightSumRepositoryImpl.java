package ru.practicum.ewm.repository;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class MinWeightSumRepositoryImpl implements MinWeightSumRepository {
    // Map<Event A, Map<Event B, Min Sum>>`
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public double get(long eventAId, long eventBId) {
        long first = Math.min(eventAId, eventBId);
        long second = Math.max(eventAId, eventBId);
        return minWeightsSums
                .computeIfAbsent(first, eventId -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    @Override
    public void put(long eventAId, long eventBId, double sum) {
        long first  = Math.min(eventAId, eventBId);
        long second = Math.max(eventAId, eventBId);
        minWeightsSums
                .computeIfAbsent(first, eventId -> new HashMap<>())
                .put(second, sum);
    }
}
