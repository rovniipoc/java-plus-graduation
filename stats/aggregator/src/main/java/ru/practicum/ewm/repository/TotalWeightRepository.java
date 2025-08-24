package ru.practicum.ewm.repository;

public interface TotalWeightRepository {
    double get(long eventId);

    void put(long eventId, double weight);
}
