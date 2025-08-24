package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.model.RecommendedEvent;
import ru.practicum.ewm.stats.message.RecommendedEventProto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationsMapper {
    List<RecommendedEventProto> mapToProto(List<RecommendedEvent> recommendedEvents);

    RecommendedEventProto mapToProto(RecommendedEvent recommendedEvent);
}
