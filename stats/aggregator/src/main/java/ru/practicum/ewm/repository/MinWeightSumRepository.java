package ru.practicum.ewm.repository;

public interface MinWeightSumRepository {
    double get(long eventA, long eventB);

    void put(long eventA, long eventB, double sum);
}
