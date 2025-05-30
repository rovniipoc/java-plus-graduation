package ru.practicum.ewm.repository;

import java.util.Set;

public interface InteractionMatrixRepository {
    double getWeight(long eventId, long userId);

    Set<Long> getEvents(long userId);

    void put(long eventId, long userId, double weight);
}
