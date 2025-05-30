package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.model.Similarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Mapper(componentModel = "spring")
public interface SimilarityMapper {
    default Similarity mapToSimilarity(EventSimilarityAvro similarityAvro) {
        long smallerId = Math.min(similarityAvro.getEventA(), similarityAvro.getEventB());
        long biggerId = Math.max(similarityAvro.getEventA(), similarityAvro.getEventB());

        Similarity similarity = new Similarity();
        similarity.setEventAId(smallerId);
        similarity.setEventBId(biggerId);
        similarity.setScore(similarityAvro.getScore());
        similarity.setTimestamp(similarityAvro.getTimestamp());
        return similarity;
    }
}