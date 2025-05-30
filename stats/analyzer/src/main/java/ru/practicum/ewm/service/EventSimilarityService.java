package ru.practicum.ewm.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.message.RecommendedEventProto;

import java.util.List;

public interface EventSimilarityService {
    void updateOrCreateSimilarity(EventSimilarityAvro eventSimilarityAvro);

    List<RecommendedEventProto> getSimilarEvents(long userId, long sampleEventId, int limit);

    List<RecommendedEventProto> getRecommendationsForUser(long userId, int limit);
}
